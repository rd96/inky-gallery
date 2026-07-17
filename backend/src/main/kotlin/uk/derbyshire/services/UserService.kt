package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import org.http4k.config.Secret
import org.slf4j.LoggerFactory
import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.UserStatus
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.UserRepository
import uk.derbyshire.domain.users.UserLoginDetail
import java.sql.SQLException
import kotlin.uuid.Uuid

class UserService(
    private val userRepository: UserRepository,
    private val passwordHasherService: PasswordHasherService,
    private val database: DatabaseContext
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun createPendingUser(username: String, displayName: String, role: Role): Result4k<Uuid, CreateUserFailure> {
//        val passwordHash = validateAndHashPassword(password) ?: return Failure(CreateUserFailure.INVALID_PASSWORD)
        val normalisedUsername = normaliseUsername(username)
        val normalisedDisplayName = normaliseDisplayName(displayName)

        if (!validUsername(normalisedUsername)) return Failure(CreateUserFailure.INVALID_USERNAME)

        return database.transaction {
            try {
                userRepository.createUser(
                    normalisedUsername, null, normalisedDisplayName, role, UserStatus.PENDING_ACTIVATION
                ).asSuccess()
            } catch (sqlException: SQLException) {
                logger.warn("createUser failed with SQL exception", sqlException)
                Failure(CreateUserFailure.EXISTING_USER)
            }
        }
    }

    fun findUserByUsername(username: String): UserLoginDetail? =
        database.transaction {
            userRepository.findUserByUsername(normaliseUsername(username))
        }

    fun createAdminUserIfMissing(username: String, password: Secret): Boolean =
        database.transaction {
            val hasExistingAdminUser = userRepository.hasAdminUser()
            val passwordHash = validateAndHashPassword(password) ?: return@transaction false

            if (!hasExistingAdminUser) userRepository.createUser(
                username, passwordHash, username, Role.ADMIN, UserStatus.ACTIVE
            )

            !hasExistingAdminUser
        }

    fun searchAllUsers(nameSearch: String?, role: Role?, status: UserStatus?, page: Int) =
        database.transaction {
            userRepository.searchUsers(nameSearch, role, status, USER_SEARCH_LIMIT, page)
        }

    private fun validateAndHashPassword(password: Secret): String? = password.use {
        if (validPassword(it)) passwordHasherService.hash(it)
        else null
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 100
        const val MAX_USERNAME_LENGTH = 30
        val USERNAME_REGEX = Regex("^[a-z][a-z0-9._-]{1,${MAX_USERNAME_LENGTH - 2}}[a-z0-9]$")
        val USERNAME_SPECIAL_CHAR_CHECK = Regex("[._-]{2,}")

        const val USER_SEARCH_LIMIT = 50

        private fun validUsername(username: String): Boolean {
            return USERNAME_REGEX.matchEntire(username) != null && !USERNAME_SPECIAL_CHAR_CHECK.containsMatchIn(username)
        }

        private fun normaliseUsername(username: String) =
            username.trim().lowercase()

        private fun normaliseDisplayName(displayName: String) =
            displayName.trim().replace(Regex("\\s+"), " ")

        private fun validPassword(password: String) =
            password.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH
    }
}

enum class CreateUserFailure {
    INVALID_USERNAME,
    INVALID_PASSWORD,
    EXISTING_USER,
}