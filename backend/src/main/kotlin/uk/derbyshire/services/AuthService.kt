package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import org.http4k.config.Secret
import uk.derbyshire.auth.AuthenticatedUser
import uk.derbyshire.auth.PasswordHasher
import uk.derbyshire.auth.Role
import uk.derbyshire.auth.SessionTokens
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.SessionRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AuthService(
    private val sessionRepository: SessionRepository,
    private val userService: UserService,
    private val sessionTokens: SessionTokens,
    private val passwordHasher: PasswordHasher,
    private val context: DatabaseContext,
    private val clock: Clock,
) {
    fun authenticateSession(token: String): AuthenticatedUser? =
        context.transaction {
            val tokenHash = sessionTokens.hash(token)

            val sessionUser = sessionRepository.findUserBySessionTokenHash(tokenHash) ?: return@transaction null

            if (sessionUser.disabled) return@transaction null

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
        val token = sessionTokens.generate()
        val tokenHash = sessionTokens.hash(token)

        context.transaction {
            sessionRepository.createSession(userId, tokenHash, clock.now() + SESSION_EXPIRES_AFTER)
            null
        }

        return Secret(token)
    }

    fun login(username: String, password: Secret): Result4k<LoginSuccess, LoginFailure> {
        val user = userService.findUserByUsername(username) ?: return Failure(LoginFailure.NO_USER)

        val passwordMatches = password.use {
            user.passwordHash.use { storedHash ->
                passwordHasher.verify(it, storedHash)
            }
        }

        if (!passwordMatches) return Failure(LoginFailure.PASSWORD_INCORRECT)

        if (user.disabled) return Failure(LoginFailure.DISABLED)

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

    fun logout(token: String) {
        val tokenHash = sessionTokens.hash(token)

        context.transaction {
            sessionRepository.deleteByTokenHash(tokenHash)
        }
    }

    companion object {
        val SESSION_EXPIRES_AFTER = 30.days
    }
}

data class SessionUser(
    val sessionId: Uuid,
    val expiresAt: Instant,
    val userId: Uuid,
    val username: String,
    val role: Role,
    val disabled: Boolean,
)

data class LoginSuccess(
    val sessionToken: Secret,
    val user: AuthenticatedUser,
)

enum class LoginFailure {
    DISABLED,
    PASSWORD_INCORRECT,
    NO_USER,
}