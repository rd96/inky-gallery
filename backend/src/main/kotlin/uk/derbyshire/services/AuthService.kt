package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure
import org.http4k.config.Secret
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import uk.derbyshire.domain.auth.AuthenticatedUser
import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.ActivationStatus
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.ActivationTokenRepository
import uk.derbyshire.database.repositories.SessionRepository
import uk.derbyshire.domain.auth.ActivationFailure
import uk.derbyshire.domain.auth.LoginFailure
import uk.derbyshire.domain.auth.UserActivationToken
import uk.derbyshire.domain.auth.LoginSuccess
import uk.derbyshire.domain.auth.UserPendingActivation
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.uuid.Uuid

class AuthService(
    private val sessionRepository: SessionRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val userService: UserService,
    private val sessionTokenService: SessionTokenService,
    private val passwordHasherService: PasswordHasherService,
    private val context: DatabaseContext,
    private val clock: Clock,
) {
    fun authenticateSession(token: String): AuthenticatedUser? =
        context.transaction {
            val tokenHash = sessionTokenService.hash(token)

            val sessionUser = sessionRepository.findUserBySessionTokenHash(tokenHash) ?: return@transaction null

            if (sessionUser.activationStatus != ActivationStatus.ACTIVATED || !sessionUser.enabled) return@transaction null

            if (sessionUser.expiresAt < clock.now()) {
                sessionRepository.deleteSession(sessionUser.sessionId)
                return@transaction null
            }

            sessionRepository.updateLastSeen(sessionUser.sessionId, clock.now())

            AuthenticatedUser(
                sessionUser.userId,
                sessionUser.username,
                sessionUser.role,
                sessionUser.displayName,
            )
        }

    fun createSession(userId: Uuid): Secret {
        val token = sessionTokenService.generate()
        val tokenHash = sessionTokenService.hash(token)

        context.transaction {
            sessionRepository.createSession(userId, tokenHash, clock.now() + SESSION_EXPIRES_AFTER)
            null
        }

        return Secret(token)
    }

    fun login(username: String, password: Secret): Result4k<LoginSuccess, LoginFailure> = context.transaction {
        val user = userService.findUserLoginByUsername(username) ?: return@transaction Failure(LoginFailure.USER_NOT_FOUND)

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

    fun createPendingUser(username: String, displayName: String, role: Role, createdBy: Uuid): Result4k<UserPendingActivation, CreateUserFailure> {
        val userId = userService.createPendingUser(username, displayName, role).onFailure { return it }
        val token = sessionTokenService.generate()
        val tokenHash = sessionTokenService.hash(token)
        val expiresAt = clock.now() + ACTIVATION_TOKEN_EXPIRES_AFTER

        transaction {
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

    fun createUserActivationToken(userId: Uuid, createdBy: Uuid): Result4k<UserPendingActivation, String> {
        val user = userService.findUser(userId) ?: return Failure("User $userId not found")

        if (user.activationStatus != ActivationStatus.PENDING) return Failure("User $userId is already activated")
        if (!user.enabled) return Failure("User $userId is not enabled")

        val token = sessionTokenService.generate()
        val tokenHash = sessionTokenService.hash(token)
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
        val tokenHash = activationToken.use(sessionTokenService::hash)

        val activationToken = context.transaction {
            activationTokenRepository.getByTokenHash(tokenHash)
        }?: return Failure(ActivationFailure.INVALID_ACTIVATION_TOKEN)

        if (!activationToken.isValid(clock.now())) return Failure(ActivationFailure.INVALID_ACTIVATION_TOKEN)

        val user = userService.findUser(activationToken.userId) ?: return Failure(ActivationFailure.USER_NOT_FOUND)

        if (user.activationStatus != ActivationStatus.PENDING) return Failure(ActivationFailure.USER_ALREADY_ACTIVATED)
        if (!user.enabled) return Failure(ActivationFailure.USER_DISABLED)

        return UserActivationToken(
            username = user.username,
            displayName = user.displayName,
            expiresAt = activationToken.expiresAt,
        ).asSuccess()
    }

    fun activateUser(activationToken: Secret, password: Secret): Result4k<LoginSuccess, ActivationFailure> = context.transaction {
        val tokenHash = activationToken.use(sessionTokenService::hash)

        val activationToken = context.transaction {
            activationTokenRepository.getByTokenHash(tokenHash)
        }?: return@transaction Failure(ActivationFailure.INVALID_ACTIVATION_TOKEN)

        if (!activationToken.isValid(clock.now())) return@transaction Failure(ActivationFailure.INVALID_ACTIVATION_TOKEN)

        val user = userService.findUser(activationToken.userId) ?: return@transaction Failure(ActivationFailure.USER_NOT_FOUND)

        if (user.activationStatus != ActivationStatus.PENDING) return@transaction Failure(ActivationFailure.USER_ALREADY_ACTIVATED)
        if (!user.enabled) return@transaction Failure(ActivationFailure.USER_DISABLED)

        userService.activateUser(user.id, password).onFailure { return@transaction(Failure(ActivationFailure.PASSWORD_INVALID)) }

        LoginSuccess(
            sessionToken = createSession(user.id),
        ).asSuccess()
    }

    fun logout(token: String) {
        val tokenHash = sessionTokenService.hash(token)

        context.transaction {
            sessionRepository.deleteByTokenHash(tokenHash)
        }
    }

    fun revokeUserActivationTokens(userId: Uuid): Result4k<Unit, String> {
        userService.findUser(userId) ?: return Failure("User $userId not found")

        context.transaction {
            activationTokenRepository.revokeActivationTokensForUser(userId, clock.now())
        }

        return Success(Unit)
    }

    companion object {
        val SESSION_EXPIRES_AFTER = 30.days
        val ACTIVATION_TOKEN_EXPIRES_AFTER = 7.days
    }
}
