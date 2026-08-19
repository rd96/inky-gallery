package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asSuccess
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.http4k.config.Secret
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.UserRepository
import uk.derbyshire.domain.users.ActivationStatus
import uk.derbyshire.domain.users.CreateAdminFailure
import uk.derbyshire.domain.users.CreateUserFailure
import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.UpdateUserFailure
import uk.derbyshire.domain.users.UserId
import uk.derbyshire.domain.users.UserLoginDetail
import uk.derbyshire.domain.users.UserSearchResult
import uk.derbyshire.domain.users.UserSummary
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val passwordHasherService = mockk<PasswordHasherService>()
    private val database = mockk<DatabaseContext>()

    private val userService = UserService(
        userRepository = userRepository,
        passwordHasherService = passwordHasherService,
        database = database,
    )

    @BeforeEach
    fun setUp() {
        clearMocks(
            userRepository,
            passwordHasherService,
            database,
        )

        every {
            database.transaction<Any?>(any())
        } answers {
            firstArg<() -> Any?>().invoke()
        }

        every {
            userRepository.createUser(
                username = any(),
                passwordHash = null,
                displayName = any(),
                role = any(),
                activationStatus = any(),
            )
        } returns CREATED_USER_ID
    }

    @Nested
    inner class CreatePendingUser {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "abc",
                "user1",
                "user.one",
                "user_one",
                "user-one",
                "a1b",
                "abc123",
            ],
        )
        fun `accepts valid usernames`(username: String) {
            val result = userService.createPendingUser(
                username = username,
                displayName = VALID_DISPLAY_NAME,
                role = Role.USER,
            )

            assertEquals(CREATED_USER_ID.asSuccess(), result)

            verify(exactly = 1) {
                userRepository.createUser(
                    username = username,
                    passwordHash = null,
                    displayName = VALID_DISPLAY_NAME,
                    role = Role.USER,
                    activationStatus = ActivationStatus.PENDING,
                )
            }
        }

        @Test
        fun `accepts username at max length`() {
            val username =
                "a" + "b".repeat(UserService.MAX_USERNAME_LENGTH - 2) + "1"

            val result = userService.createPendingUser(
                username = username,
                displayName = VALID_DISPLAY_NAME,
                role = Role.USER,
            )

            assertEquals(CREATED_USER_ID.asSuccess(), result)

            verify(exactly = 1) {
                userRepository.createUser(
                    username = username,
                    passwordHash = null,
                    displayName = VALID_DISPLAY_NAME,
                    role = Role.USER,
                    activationStatus = ActivationStatus.PENDING,
                )
            }
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "",
                "a",
                "ab",
                "1ab",
                ".ab",
                "_ab",
                "-ab",
                "ab.",
                "ab_",
                "ab-",
                "a b",
                "a!b",
                "a/b",
                "a:b",
                "a--b",
                "a..b",
                "a__b",
                "a-.b",
                "a-_b",
                "a-b-c__d",
            ],
        )
        fun `rejects invalid usernames`(username: String) {
            val result = userService.createPendingUser(
                username = username,
                displayName = VALID_DISPLAY_NAME,
                role = Role.USER,
            )

            assertEquals(
                Failure(CreateUserFailure.INVALID_USERNAME),
                result,
            )

            verify(exactly = 0) {
                userRepository.createUser(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `rejects username longer than max length`() {
            val username =
                "a" + "b".repeat(UserService.MAX_USERNAME_LENGTH - 1) + "1"

            val result = userService.createPendingUser(
                username = username,
                displayName = VALID_DISPLAY_NAME,
                role = Role.USER,
            )

            assertEquals(
                Failure(CreateUserFailure.INVALID_USERNAME),
                result,
            )

            verify(exactly = 0) {
                userRepository.createUser(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `normalises username before saving`() {
            val result = userService.createPendingUser(
                username = "  User-One  ",
                displayName = VALID_DISPLAY_NAME,
                role = Role.USER,
            )

            assertEquals(CREATED_USER_ID.asSuccess(), result)

            verify(exactly = 1) {
                userRepository.createUser(
                    username = "user-one",
                    passwordHash = null,
                    displayName = VALID_DISPLAY_NAME,
                    role = Role.USER,
                    activationStatus = ActivationStatus.PENDING,
                )
            }
        }

        @Test
        fun `normalises display name before saving`() {
            val result = userService.createPendingUser(
                username = "user-one",
                displayName = "  Valid   Display   Name  ",
                role = Role.USER,
            )

            assertEquals(CREATED_USER_ID.asSuccess(), result)

            verify(exactly = 1) {
                userRepository.createUser(
                    username = "user-one",
                    passwordHash = null,
                    displayName = "Valid Display Name",
                    role = Role.USER,
                    activationStatus = ActivationStatus.PENDING,
                )
            }
        }

        @Test
        fun `rejects display name shorter than minimum length`() {
            val result = userService.createPendingUser(
                username = "valid-user",
                displayName = " A ",
                role = Role.USER,
            )

            assertEquals(
                Failure(CreateUserFailure.INVALID_DISPLAY_NAME),
                result,
            )

            verify(exactly = 0) {
                userRepository.createUser(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `rejects display name longer than maximum length`() {
            val result = userService.createPendingUser(
                username = "valid-user",
                displayName = "a".repeat(
                    UserService.MAX_DISPLAY_NAME_LENGTH + 1,
                ),
                role = Role.USER,
            )

            assertEquals(
                Failure(CreateUserFailure.INVALID_DISPLAY_NAME),
                result,
            )

            verify(exactly = 0) {
                userRepository.createUser(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `returns username already in use when insert is ignored`() {
            every {
                userRepository.createUser(
                    username = "existing-user",
                    passwordHash = null,
                    displayName = VALID_DISPLAY_NAME,
                    role = Role.USER,
                    activationStatus = ActivationStatus.PENDING,
                )
            } returns null

            val result = userService.createPendingUser(
                username = "existing-user",
                displayName = VALID_DISPLAY_NAME,
                role = Role.USER,
            )

            assertEquals(
                Failure(CreateUserFailure.USERNAME_ALREADY_IN_USE),
                result,
            )
        }
    }

    @Nested
    inner class FindUsers {
        @Test
        fun `normalises username before login lookup`() {
            every {
                userRepository.findUserLoginByUsername("user-one")
            } returns FOUND_LOGIN_USER

            val result =
                userService.findUserLoginByUsername("  User-One  ")

            assertEquals(FOUND_LOGIN_USER, result)

            verify(exactly = 1) {
                userRepository.findUserLoginByUsername("user-one")
            }
        }

        @Test
        fun `finds user by id`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns STANDARD_USER

            val result = userService.findUser(CREATED_USER_ID)

            assertEquals(STANDARD_USER, result)
        }

        @Test
        fun `returns whether a user exists`() {
            every {
                userRepository.userExists(CREATED_USER_ID)
            } returns true

            val result = userService.userExists(CREATED_USER_ID)

            assertEquals(true, result)
        }

        @Test
        fun `returns whether an admin exists`() {
            every {
                userRepository.hasAdminUser()
            } returns true

            val result = userService.hasAdminUser()

            assertEquals(true, result)
        }
    }

    @Nested
    inner class CreateInitialAdminUser {
        @Test
        fun `creates activated admin`() {
            every {
                userRepository.hasAdminUser()
            } returns false

            every {
                passwordHasherService.validateAndHashPassword(VALID_PASSWORD)
            } returns HASHED_PASSWORD

            every {
                userRepository.createUser(
                    username = "admin-user",
                    passwordHash = HASHED_PASSWORD,
                    displayName = "admin-user",
                    role = Role.ADMIN,
                    activationStatus = ActivationStatus.ACTIVATED,
                )
            } returns CREATED_USER_ID

            val result = userService.createInitialAdminUser(
                username = "  Admin-User  ",
                password = Secret(VALID_PASSWORD),
            )

            assertEquals(Unit.asSuccess(), result)

            verify(exactly = 1) {
                userRepository.createUser(
                    username = "admin-user",
                    passwordHash = HASHED_PASSWORD,
                    displayName = "admin-user",
                    role = Role.ADMIN,
                    activationStatus = ActivationStatus.ACTIVATED,
                )
            }
        }

        @Test
        fun `rejects invalid username before hashing password`() {
            val result = userService.createInitialAdminUser(
                username = "a",
                password = Secret(VALID_PASSWORD),
            )

            assertEquals(
                Failure(CreateAdminFailure.INVALID_USERNAME),
                result,
            )

            verify(exactly = 0) {
                passwordHasherService.validateAndHashPassword(any())
            }

            verify(exactly = 0) {
                userRepository.hasAdminUser()
            }
        }

        @Test
        fun `rejects an invalid password`() {
            every {
                passwordHasherService.validateAndHashPassword(any())
            } returns null

            val result = userService.createInitialAdminUser(
                username = "admin-user",
                password = Secret("too-short-or-too-long"),
            )

            assertEquals(
                Failure(CreateAdminFailure.INVALID_PASSWORD),
                result,
            )

            verify(exactly = 0) {
                userRepository.hasAdminUser()
            }

            verify(exactly = 0) {
                userRepository.createUser(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `returns admin already exists when an admin exists`() {
            every {
                passwordHasherService.validateAndHashPassword(VALID_PASSWORD)
            } returns HASHED_PASSWORD

            every {
                userRepository.hasAdminUser()
            } returns true

            val result = userService.createInitialAdminUser(
                username = "admin-user",
                password = Secret(VALID_PASSWORD),
            )

            assertEquals(
                Failure(CreateAdminFailure.ADMIN_ALREADY_EXISTS),
                result,
            )

            verify(exactly = 0) {
                userRepository.createUser(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `returns username taken when insert is ignored`() {
            every {
                passwordHasherService.validateAndHashPassword(VALID_PASSWORD)
            } returns HASHED_PASSWORD

            every {
                userRepository.hasAdminUser()
            } returns false

            every {
                userRepository.createUser(
                    username = "admin-user",
                    passwordHash = HASHED_PASSWORD,
                    displayName = "admin-user",
                    role = Role.ADMIN,
                    activationStatus = ActivationStatus.ACTIVATED,
                )
            } returns null

            val result = userService.createInitialAdminUser(
                username = "admin-user",
                password = Secret(VALID_PASSWORD),
            )

            assertEquals(
                Failure(CreateAdminFailure.USERNAME_TAKEN),
                result,
            )
        }
    }

    @Nested
    inner class SearchAllUsers {
        @Test
        fun `clamps page below one to page one`() {
            val expected = UserSearchResult(
                users = emptyList(),
                totalCount = 0,
            )

            every {
                userRepository.searchUsers(
                    nameSearch = "user",
                    role = Role.USER,
                    activationStatus = ActivationStatus.PENDING,
                    enabled = true,
                    limit = UserService.USER_SEARCH_LIMIT,
                    page = 1,
                )
            } returns expected

            val result = userService.searchAllUsers(
                nameSearch = "user",
                role = Role.USER,
                activationStatus = ActivationStatus.PENDING,
                enabled = true,
                page = -5,
            )

            assertEquals(expected, result)
        }

        @Test
        fun `passes valid page through unchanged`() {
            val expected = UserSearchResult(
                users = emptyList(),
                totalCount = 0,
            )

            every {
                userRepository.searchUsers(
                    nameSearch = null,
                    role = null,
                    activationStatus = null,
                    enabled = null,
                    limit = UserService.USER_SEARCH_LIMIT,
                    page = 3,
                )
            } returns expected

            val result = userService.searchAllUsers(
                nameSearch = null,
                role = null,
                activationStatus = null,
                enabled = null,
                page = 3,
            )

            assertEquals(expected, result)
        }
    }

    @Nested
    inner class UpdateUserDisplayName {
        @Test
        fun `rejects display name shorter than minimum length`() {
            val result = userService.updateUserDisplayName(
                userId = CREATED_USER_ID,
                displayName = " A ",
            )

            assertEquals(
                Failure(UpdateUserFailure.INVALID_DISPLAY_NAME),
                result,
            )

            verify(exactly = 0) {
                userRepository.findUser(any())
            }
        }

        @Test
        fun `rejects display name longer than maximum length`() {
            val result = userService.updateUserDisplayName(
                userId = CREATED_USER_ID,
                displayName = "a".repeat(
                    UserService.MAX_DISPLAY_NAME_LENGTH + 1,
                ),
            )

            assertEquals(
                Failure(UpdateUserFailure.INVALID_DISPLAY_NAME),
                result,
            )

            verify(exactly = 0) {
                userRepository.findUser(any())
            }
        }

        @Test
        fun `returns user not found when user does not exist`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns null

            val result = userService.updateUserDisplayName(
                userId = CREATED_USER_ID,
                displayName = "New Display Name",
            )

            assertEquals(
                Failure(UpdateUserFailure.USER_NOT_FOUND),
                result,
            )

            verify(exactly = 0) {
                userRepository.updateUserDisplayName(any(), any())
            }
        }

        @Test
        fun `does nothing when display name is unchanged`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns STANDARD_USER

            val result = userService.updateUserDisplayName(
                userId = CREATED_USER_ID,
                displayName = STANDARD_USER.displayName,
            )

            assertEquals(Unit.asSuccess(), result)

            verify(exactly = 0) {
                userRepository.updateUserDisplayName(any(), any())
            }
        }

        @Test
        fun `normalises and saves a changed display name`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns STANDARD_USER

            every {
                userRepository.updateUserDisplayName(
                    CREATED_USER_ID,
                    "New Display Name",
                )
            } returns Success(Unit)

            val result = userService.updateUserDisplayName(
                userId = CREATED_USER_ID,
                displayName = "  New   Display   Name  ",
            )

            assertEquals(Unit.asSuccess(), result)

            verify(exactly = 1) {
                userRepository.updateUserDisplayName(
                    CREATED_USER_ID,
                    "New Display Name",
                )
            }
        }
    }

    @Nested
    inner class UpdateUser {
        @Test
        fun `rejects invalid username before lookup`() {
            val result = userService.updateUser(
                userId = CREATED_USER_ID,
                username = "a",
                displayName = null,
                enabled = null,
                role = null,
            )

            assertEquals(
                Failure(UpdateUserFailure.INVALID_USERNAME),
                result,
            )

            verify(exactly = 0) {
                userRepository.findUser(any())
            }
        }

        @Test
        fun `rejects invalid display name before lookup`() {
            val result = userService.updateUser(
                userId = CREATED_USER_ID,
                username = null,
                displayName = " A ",
                enabled = null,
                role = null,
            )

            assertEquals(
                Failure(UpdateUserFailure.INVALID_DISPLAY_NAME),
                result,
            )

            verify(exactly = 0) {
                userRepository.findUser(any())
            }
        }

        @Test
        fun `returns user not found when user does not exist`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns null

            val result = userService.updateUser(
                userId = CREATED_USER_ID,
                username = "new-username",
                displayName = null,
                enabled = null,
                role = null,
            )

            assertEquals(
                Failure(UpdateUserFailure.USER_NOT_FOUND),
                result,
            )

            verify(exactly = 0) {
                userRepository.updateUser(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `accepts empty update when user exists`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns STANDARD_USER

            val result = userService.updateUser(
                userId = CREATED_USER_ID,
                username = null,
                displayName = null,
                enabled = null,
                role = null,
            )

            assertEquals(Unit.asSuccess(), result)

            verify(exactly = 0) {
                userRepository.updateUser(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `normalises supplied username and display name`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns STANDARD_USER

            every {
                userRepository.updateUser(
                    userId = CREATED_USER_ID,
                    username = "new-username",
                    displayName = "New Display Name",
                    enabled = null,
                    role = null,
                )
            } returns Success(Unit)

            val result = userService.updateUser(
                userId = CREATED_USER_ID,
                username = "  New-Username  ",
                displayName = "  New   Display   Name  ",
                enabled = null,
                role = null,
            )

            assertEquals(Unit.asSuccess(), result)

            verify(exactly = 1) {
                userRepository.updateUser(
                    userId = CREATED_USER_ID,
                    username = "new-username",
                    displayName = "New Display Name",
                    enabled = null,
                    role = null,
                )
            }
        }

        @Test
        fun `prevents disabling sole enabled admin`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns ADMIN_USER

            every {
                userRepository.countEnabledAdmins()
            } returns 1

            val result = userService.updateUser(
                userId = CREATED_USER_ID,
                username = null,
                displayName = null,
                enabled = false,
                role = null,
            )

            assertEquals(
                Failure(UpdateUserFailure.CANNOT_DEMOTE_LAST_ADMIN),
                result,
            )

            verify(exactly = 0) {
                userRepository.updateUser(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `prevents demoting sole enabled admin`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns ADMIN_USER

            every {
                userRepository.countEnabledAdmins()
            } returns 1

            val result = userService.updateUser(
                userId = CREATED_USER_ID,
                username = null,
                displayName = null,
                enabled = null,
                role = Role.USER,
            )

            assertEquals(
                Failure(UpdateUserFailure.CANNOT_DEMOTE_LAST_ADMIN),
                result,
            )

            verify(exactly = 0) {
                userRepository.updateUser(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `allows disabling admin when another enabled admin exists`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns ADMIN_USER

            every {
                userRepository.countEnabledAdmins()
            } returns 2

            every {
                userRepository.updateUser(
                    userId = CREATED_USER_ID,
                    username = null,
                    displayName = null,
                    enabled = false,
                    role = null,
                )
            } returns Success(Unit)

            val result = userService.updateUser(
                userId = CREATED_USER_ID,
                username = null,
                displayName = null,
                enabled = false,
                role = null,
            )

            assertEquals(Unit.asSuccess(), result)
        }

        @Test
        fun `does not count admins for unrelated admin update`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns ADMIN_USER

            every {
                userRepository.updateUser(
                    userId = CREATED_USER_ID,
                    username = null,
                    displayName = "Updated Admin",
                    enabled = null,
                    role = null,
                )
            } returns Success(Unit)

            val result = userService.updateUser(
                userId = CREATED_USER_ID,
                username = null,
                displayName = "Updated Admin",
                enabled = null,
                role = null,
            )

            assertEquals(Unit.asSuccess(), result)

            verify(exactly = 0) {
                userRepository.countEnabledAdmins()
            }
        }

        @Test
        fun `does not count admins when role is kept as admin`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns ADMIN_USER

            every {
                userRepository.updateUser(
                    userId = CREATED_USER_ID,
                    username = null,
                    displayName = null,
                    enabled = null,
                    role = Role.ADMIN,
                )
            } returns Success(Unit)

            val result = userService.updateUser(
                userId = CREATED_USER_ID,
                username = null,
                displayName = null,
                enabled = null,
                role = Role.ADMIN,
            )

            assertEquals(Unit.asSuccess(), result)

            verify(exactly = 0) {
                userRepository.countEnabledAdmins()
            }
        }

        @Test
        fun `returns the repository failure when the update fails`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns STANDARD_USER

            every {
                userRepository.updateUser(
                    userId = CREATED_USER_ID,
                    username = "new-username",
                    displayName = null,
                    enabled = null,
                    role = null,
                )
            } returns Failure(UpdateUserFailure.USERNAME_ALREADY_IN_USE)

            val result = userService.updateUser(
                userId = CREATED_USER_ID,
                username = "new-username",
                displayName = null,
                enabled = null,
                role = null,
            )

            assertEquals(
                Failure(UpdateUserFailure.USERNAME_ALREADY_IN_USE),
                result,
            )
        }
    }

    private companion object {
        const val VALID_PASSWORD = "valid-password"
        const val HASHED_PASSWORD = "hashed-password"
        const val VALID_DISPLAY_NAME = "Valid Display Name"

        val CREATED_USER_ID = UserId(
            Uuid.parse("00000000-0000-0000-0000-000000000001"),
        )

        val FOUND_LOGIN_USER = UserLoginDetail(
            id = UserId(
                Uuid.parse("00000000-0000-0000-0000-000000000002"),
            ),
            username = "user-one",
            passwordHash = Secret(HASHED_PASSWORD),
            role = Role.USER,
            activationStatus = ActivationStatus.PENDING,
            enabled = true,
        )

        val STANDARD_USER = UserSummary(
            id = CREATED_USER_ID,
            username = "standard-user",
            displayName = "Standard User",
            role = Role.USER,
            activationStatus = ActivationStatus.ACTIVATED,
            enabled = true,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )

        val ADMIN_USER = UserSummary(
            id = CREATED_USER_ID,
            username = "admin-user",
            displayName = "Admin User",
            role = Role.ADMIN,
            activationStatus = ActivationStatus.ACTIVATED,
            enabled = true,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )
    }
}
