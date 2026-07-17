package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure
import org.http4k.config.Secret
import uk.derbyshire.domain.auth.AuthenticatedUser
import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.UserStatus
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.ActivationTokenRepository
import uk.derbyshire.database.repositories.SessionRepository
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

            if (sessionUser.status != UserStatus.ACTIVE) return@transaction null

            if (sessionUser.expiresAt < clock.now()) {
                sessionRepository.deleteSession(sessionUser.sessionId)
                return@transaction null
            }

            sessionRepository.updateLastSeen(sessionUser.sessionId, clock.now())

            AuthenticatedUser(
                sessionUser.userId,
                sessionUser.username,
                sessionUser.role,
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

    fun login(username: String, password: Secret): Result4k<LoginSuccess, LoginFailure> {
        val user = userService.findUserByUsername(username) ?: return Failure(LoginFailure.NO_USER)

        if (user.status != UserStatus.ACTIVE || user.passwordHash == null) return Failure(LoginFailure.DISABLED)

        val passwordMatches = password.use {
            user.passwordHash.use { storedHash ->
                passwordHasherService.verify(it, storedHash)
            }
        }

        if (!passwordMatches) return Failure(LoginFailure.PASSWORD_INCORRECT)

        val sessionToken = createSession(user.id)

        return LoginSuccess(
            sessionToken,
            AuthenticatedUser(
                user.id,
                user.username,
                user.role,
            ),
        ).asSuccess()
    }

    fun createPendingUser(username: String, displayName: String, role: Role, createdBy: Uuid): Result4k<UserPendingActivation, String> {
        val userId = userService.createPendingUser(username, displayName, role).onFailure { return Failure("Failed to create pending user $username") }
        val token = sessionTokenService.generate()
        val tokenHash = sessionTokenService.hash(token)

        activationTokenRepository.createActivationToken(
            userId,
            tokenHash,
            createdBy,
            clock.now() + ACTIVATION_TOKEN_EXPIRES_AFTER,
        )

        return UserPendingActivation(
            userId = userId,
            activationToken = token,
        ).asSuccess()
    }

    fun getActivationDetails(activationToken: Secret) {

    }

    fun activate(username: String, activationToken: Secret, password: Secret): Result4k<LoginSuccess, LoginFailure> {
        val user = userService.findUserByUsername(username) ?: return Failure(LoginFailure.NO_USER)

        if (user.status != UserStatus.PENDING_ACTIVATION || user.passwordHash != null) return Failure(LoginFailure.USER_ALREADY_ACTIVATED)

        return LoginFailure.DISABLED.asFailure()
    }

    fun logout(token: String) {
        val tokenHash = sessionTokenService.hash(token)

        context.transaction {
            sessionRepository.deleteByTokenHash(tokenHash)
        }
    }

    companion object {
        val SESSION_EXPIRES_AFTER = 30.days
        val ACTIVATION_TOKEN_EXPIRES_AFTER = 7.days
    }
}

data class CreateUserActivationLink(
    val userId: Uuid,
    val token: String,
)

enum class LoginFailure {
    DISABLED,
    PASSWORD_INCORRECT,
    NO_USER,
    USER_ALREADY_ACTIVATED,
}