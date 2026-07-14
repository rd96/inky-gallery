package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import org.http4k.config.Secret
import org.slf4j.LoggerFactory
import uk.derbyshire.auth.PasswordHasher
import uk.derbyshire.auth.Role
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.UserRepository
import java.sql.SQLException
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UserService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val database: DatabaseContext
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    fun createUser(username: String, password: Secret): Result4k<Uuid, CreateUserFailure> {
        val passwordHash = validateAndHashPassword(password) ?: return Failure(CreateUserFailure.INVALID_PASSWORD)
        val normalisedUsername = normaliseUsername(username)

        if (!validUsername(normalisedUsername)) return Failure(CreateUserFailure.INVALID_USERNAME)

        return database.transaction {
            try {
                userRepository.createUser(normalisedUsername, passwordHash).asSuccess()
            } catch (sqlException: SQLException) {
                logger.warn("createUser failed with SQL exception", sqlException)
                Failure(CreateUserFailure.EXISTING_USER)
            }
        }
    }

    fun findUserByUsername(username: String): User? =
        database.transaction {
            userRepository.findUserByUsername(normaliseUsername(username))
        }

    fun createAdminUserIfMissing(username: String, password: Secret): Boolean =
        database.transaction {
            val hasExistingAdminUser = userRepository.hasAdminUser()
            val passwordHash = validateAndHashPassword(password) ?: return@transaction false

            if (!hasExistingAdminUser) userRepository.createAdminUser(username, passwordHash)

            hasExistingAdminUser
        }

    private fun validateAndHashPassword(password: Secret): String? = password.use {
        val isValid = it.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH

        if (isValid) passwordHasher.hash(it)
        else null
    }

    private fun validUsername(username: String): Boolean {
        return USERNAME_REGEX.matchEntire(username) != null && !USERNAME_SPECIAL_CHAR_CHECK.containsMatchIn(username)
    }

    private fun normaliseUsername(username: String): String {
        return username.trim().lowercase()
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 100
        const val MAX_USERNAME_LENGTH = 30
        val USERNAME_REGEX = Regex("^[a-z][a-z0-9._-]{1,${MAX_USERNAME_LENGTH - 2}}[a-z0-9]$")
        val USERNAME_SPECIAL_CHAR_CHECK = Regex("[._-]{2,}")
    }
}

data class User(
    val id: Uuid,
    val username: String,
    val passwordHash: Secret,
    val role: Role,
    val createdAt: Instant,
    val disabled: Boolean,
)

enum class CreateUserFailure {
    INVALID_USERNAME,
    INVALID_PASSWORD,
    EXISTING_USER,
}