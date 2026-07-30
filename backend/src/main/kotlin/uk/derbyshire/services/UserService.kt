package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asResultOr
import org.http4k.config.Secret
import uk.derbyshire.domain.users.Role
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.UserRepository
import uk.derbyshire.domain.users.ActivationStatus
import uk.derbyshire.domain.users.CreateAdminFailure
import uk.derbyshire.domain.users.CreateUserFailure
import uk.derbyshire.domain.users.UpdateUserFailure
import uk.derbyshire.domain.users.UserId
import uk.derbyshire.domain.users.UserLoginDetail
import uk.derbyshire.domain.users.UserSearchResult
import uk.derbyshire.domain.users.UserSummary
import kotlin.math.max

class UserService(
    private val userRepository: UserRepository,
    private val passwordHasherService: PasswordHasherService,
    private val database: DatabaseContext
) {
    fun createPendingUser(username: String, displayName: String, role: Role): Result4k<UserId, CreateUserFailure> {
        val normalisedUsername = normaliseUsername(username)
        val normalisedDisplayName = normaliseDisplayName(displayName)

        if (!validUsername(normalisedUsername)) return Failure(CreateUserFailure.INVALID_USERNAME)
        if (!validDisplayName(normalisedDisplayName)) return Failure(CreateUserFailure.INVALID_DISPLAY_NAME)

        return database.transaction {
            userRepository.createUser(
                normalisedUsername, null, normalisedDisplayName, role, ActivationStatus.PENDING
            )
        }.asResultOr { CreateUserFailure.USERNAME_ALREADY_IN_USE }
    }

    fun findUserLoginByUsername(username: String): UserLoginDetail? =
        database.transaction {
            userRepository.findUserLoginByUsername(normaliseUsername(username))
        }

    fun userExists(userId: UserId): Boolean =
        database.transaction {
            userRepository.userExists(userId)
        }

    fun findUser(userId: UserId): UserSummary? =
        database.transaction {
            userRepository.findUser(userId)
        }

    fun hasAdminUser(): Boolean = database.transaction {
        userRepository.hasAdminUser()
    }

    fun createInitialAdminUser(username: String, password: Secret): Result4k<Unit, CreateAdminFailure> {
        val normalisedUsername = normaliseUsername(username)
        val normalisedDisplayName = normaliseDisplayName(normalisedUsername)

        if (!validUsername(normalisedUsername)) return Failure(CreateAdminFailure.INVALID_USERNAME)
        if (!validDisplayName(normalisedDisplayName)) return Failure(CreateAdminFailure.INVALID_DISPLAY_NAME)

        val passwordHash = password.use(passwordHasherService::validateAndHashPassword) ?: return Failure(CreateAdminFailure.INVALID_PASSWORD)

        return database.transaction {
            if (userRepository.hasAdminUser()) return@transaction Failure(CreateAdminFailure.ADMIN_ALREADY_EXISTS)

            userRepository.createUser(
                normalisedUsername, passwordHash, normalisedDisplayName, Role.ADMIN, ActivationStatus.ACTIVATED
            ) ?: return@transaction Failure(CreateAdminFailure.USERNAME_TAKEN)

            Success(Unit)
        }
    }

    fun searchAllUsers(nameSearch: String?, role: Role?, activationStatus: ActivationStatus?, enabled: Boolean?, page: Int): UserSearchResult =
        database.transaction {
            userRepository.searchUsers(nameSearch, role, activationStatus, enabled, USER_SEARCH_LIMIT, max(1, page))
        }

    fun updateUser(userId: UserId, username: String?, displayName: String?, enabled: Boolean?, role: Role?): Result4k<Unit, UpdateUserFailure> {
        val normalisedUsername = username?.let(::normaliseUsername)
        val normalisedDisplayName = displayName?.let(::normaliseDisplayName)

        if (normalisedUsername != null && !validUsername(normalisedUsername)) return Failure(UpdateUserFailure.INVALID_USERNAME)
        if (normalisedDisplayName != null && !validDisplayName(normalisedDisplayName)) return Failure(UpdateUserFailure.INVALID_DISPLAY_NAME)

        return database.transaction {
            val user = userRepository.findUser(userId) ?: return@transaction Failure(UpdateUserFailure.USER_NOT_FOUND)
            if (username == null && displayName == null && enabled == null && role == null) return@transaction Success(Unit)

            if (user.enabled && user.role == Role.ADMIN && (enabled == false || (role != null && role != Role.ADMIN))) {
                // potential race condition here, ignoring because of the small user scope of the app
                if (userRepository.countEnabledAdmins() == 1L) return@transaction Failure(UpdateUserFailure.CANNOT_DEMOTE_LAST_ADMIN)
            }

            userRepository.updateUser(
                userId,
                normalisedUsername,
                normalisedDisplayName,
                enabled,
                role,
            )
        }
    }

    companion object {
        const val MAX_USERNAME_LENGTH = 30

        const val MIN_DISPLAY_NAME_LENGTH = 2
        const val MAX_DISPLAY_NAME_LENGTH = 30

        private val USERNAME_REGEX = Regex("^[a-z][a-z0-9._-]{1,${MAX_USERNAME_LENGTH - 2}}[a-z0-9]$")
        private val USERNAME_SPECIAL_CHAR_CHECK = Regex("[._-]{2,}")
        private val DISPLAY_NAME_WHITESPACE_REGEX = Regex("\\s+")

        const val USER_SEARCH_LIMIT = 50

        private fun validUsername(username: String) =
            USERNAME_REGEX.matchEntire(username) != null && !USERNAME_SPECIAL_CHAR_CHECK.containsMatchIn(username)

        private fun validDisplayName(displayName: String): Boolean =
            displayName.length in MIN_DISPLAY_NAME_LENGTH..MAX_DISPLAY_NAME_LENGTH

        private fun normaliseUsername(username: String) =
            username.trim().lowercase()

        private fun normaliseDisplayName(displayName: String) =
            displayName.trim().replace(DISPLAY_NAME_WHITESPACE_REGEX, " ")


    }
}

