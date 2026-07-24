package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure
import org.http4k.config.Secret
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import uk.derbyshire.domain.auth.AuthenticatedUser
import uk.derbyshire.domain.users.ActivationStatus
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.ActivationTokenRepository
import uk.derbyshire.database.repositories.DeviceApiKeyRepository
import uk.derbyshire.database.repositories.SessionRepository
import uk.derbyshire.database.repositories.UserRepository
import uk.derbyshire.domain.auth.ActivationFailure
import uk.derbyshire.domain.auth.AuthenticatedDevice
import uk.derbyshire.domain.auth.LoginFailure
import uk.derbyshire.domain.auth.UserActivationToken
import uk.derbyshire.domain.auth.LoginSuccess
import uk.derbyshire.domain.auth.UserPendingActivation
import uk.derbyshire.domain.users.UserId
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class AuthService(
    private val sessionRepository: SessionRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val apiKeyRepository: DeviceApiKeyRepository,
    private val userRepository: UserRepository,
    private val secureTokenService: SecureTokenService,
    private val passwordHasherService: PasswordHasherService,
    private val context: DatabaseContext,
    private val clock: Clock,
) {
    fun authenticateSession(token: String): AuthenticatedUser? =
        context.transaction {
            val tokenHash = secureTokenService.hash(token)

            val sessionUser = sessionRepository.findUserBySessionTokenHash(tokenHash) ?: return@transaction null

            if (sessionUser.activationStatus != ActivationStatus.ACTIVATED || !sessionUser.enabled) return@transaction null

            if (sessionUser.expiresAt < clock.now()) {
                sessionRepository.deleteSession(sessionUser.sessionId)
                return@transaction null
            }

            if (sessionUser.lastSeen < clock.now().minus(1.minutes)) {
                sessionRepository.updateLastSeen(sessionUser.sessionId, clock.now())
            }

            AuthenticatedUser(
                sessionUser.userId,
                sessionUser.username,
                sessionUser.role,
                sessionUser.displayName,
            )
        }

    fun authenticateApiKey(apiKey: String): AuthenticatedDevice? =
        context.transaction {
            val apiKeyHash = secureTokenService.hash(apiKey)

            val apiKeyUser = apiKeyRepository.findUserByApiKeyHash(apiKeyHash) ?: return@transaction null

            if (apiKeyUser.revokedAt != null || !apiKeyUser.userEnabled || !apiKeyUser.deviceEnabled) return@transaction null

            AuthenticatedDevice(
                userId = apiKeyUser.userId,
                deviceId = apiKeyUser.deviceId,
            )
        }

    fun createSession(userId: UserId): Secret {
        val token = secureTokenService.generate()
        val tokenHash = secureTokenService.hash(token)

        context.transaction {
            sessionRepository.createSession(userId, tokenHash, clock.now() + SESSION_EXPIRES_AFTER)
            null
        }

        return Secret(token)
    }

    fun login(username: String, password: Secret): Result4k<LoginSuccess, LoginFailure> = context.transaction {
        val user = userRepository.findUserLoginByUsername(username) ?: return@transaction Failure(LoginFailure.USER_NOT_FOUND)

        if (user.activationStatus == ActivationStatus.PENDING || user.passwordHash == null) return@transaction Failure(LoginFailure.USER_PENDING_ACTIVATION)
        if (!user.enabled) return@transaction Failure(LoginFailure.USER_DISABLED)

        val passwordMatches = password.use {
            user.passwordHash.use { storedHash ->
                passwordHasherService.verify(it, storedHash)
            }
        }

        if (!passwordMatches) return@transaction Failure(LoginFailure.PASSWORD_INCORRECT)

        val sessionToken = createSession(user.id)

        LoginSuccess(
            sessionToken,
        ).asSuccess()
    }

    fun generateUserActivationToken(userId: UserId, createdBy: UserId): Result4k<UserPendingActivation, String> {
        val user = userRepository.findUser(userId) ?: return Failure("User $userId not found")

        if (user.activationStatus != ActivationStatus.PENDING) return Failure("User $userId is already activated")
        if (!user.enabled) return Failure("User $userId is not enabled")

        val token = secureTokenService.generate()
        val tokenHash = secureTokenService.hash(token)
        val expiresAt = clock.now() + ACTIVATION_TOKEN_EXPIRES_AFTER

        transaction {
            activationTokenRepository.revokeActivationTokensForUser(userId, clock.now())
            activationTokenRepository.createActivationToken(
                userId,
                tokenHash,
                expiresAt,
                createdBy,
                clock.now(),
            )
        }

        return UserPendingActivation(
            userId = userId,
            activationToken = token,
            expiresAt = expiresAt,
        ).asSuccess()
    }

    fun getActivationDetails(activationToken: Secret): Result4k<UserActivationToken, ActivationFailure> {
        val tokenHash = activationToken.use(secureTokenService::hash)

        return getAndValidateActivationToken(tokenHash)
    }

    fun activateUser(activationToken: Secret, password: Secret): Result4k<LoginSuccess, ActivationFailure> {
        val tokenHash = activationToken.use(secureTokenService::hash)
        val passwordHash = password.use(passwordHasherService::validateAndHashPassword) ?: return Failure(ActivationFailure.PASSWORD_INVALID)

        return context.transaction {
            val activationToken = getAndValidateActivationToken(tokenHash).onFailure { return@transaction it }

            userRepository.setPendingUserPasswordAndStatusActivated(activationToken.userId, passwordHash).onFailure { return@transaction it }

            LoginSuccess(
                sessionToken = createSession(activationToken.userId),
            ).asSuccess()
        }
    }

    private fun getAndValidateActivationToken(tokenHash: String): Result4k<UserActivationToken, ActivationFailure> =
        context.transaction {
            val activationToken = activationTokenRepository.getByTokenHash(tokenHash) ?: return@transaction Failure(ActivationFailure.INVALID_ACTIVATION_TOKEN)
            if (!activationToken.isValid(clock.now())) return@transaction Failure(ActivationFailure.INVALID_ACTIVATION_TOKEN)

            val user = userRepository.findUser(activationToken.userId) ?: return@transaction Failure(ActivationFailure.PENDING_USER_NOT_FOUND)

            if (user.activationStatus != ActivationStatus.PENDING) return@transaction Failure(ActivationFailure.USER_ALREADY_ACTIVATED)
            if (!user.enabled) return@transaction Failure(ActivationFailure.USER_DISABLED)

            UserActivationToken(
                userId = user.id,
                username = user.username,
                displayName = user.displayName,
                expiresAt = activationToken.expiresAt,
            ).asSuccess()
        }

    fun logout(token: String) {
        val tokenHash = secureTokenService.hash(token)

        context.transaction {
            sessionRepository.deleteByTokenHash(tokenHash)
        }
    }

    fun revokeUserActivationTokens(userId: UserId): Result4k<Unit, String> =
        context.transaction {
            if (!userRepository.userExists(userId)) return@transaction Failure("User $userId not found")
            activationTokenRepository.revokeActivationTokensForUser(userId, clock.now()).asSuccess()
        }

    fun disableUserAndRevokeSessions(userId: UserId) {
        context.transaction {
            userRepository.disableUser(userId)
            sessionRepository.deleteSessionsForUser(userId)
        }
    }

    companion object {
        val SESSION_EXPIRES_AFTER = 30.days
        val ACTIVATION_TOKEN_EXPIRES_AFTER = 7.days
    }
}
