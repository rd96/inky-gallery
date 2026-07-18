package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.http4k.config.Secret
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.derbyshire.domain.users.Role
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.UserRepository
import uk.derbyshire.domain.users.ActivationStatus
import uk.derbyshire.domain.users.UserLoginDetail
import java.sql.SQLException
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
        clearMocks(userRepository, passwordHasherService, database)

        every { passwordHasherService.hash(any()) } returns HASHED_PASSWORD

        every {
            database.transaction<CreateUserResult>(any())
        } answers {
            firstArg<() -> CreateUserResult>().invoke()
        }

        every {
            userRepository.createUser(any(), null, any(), any(), any())
        } returns CREATED_USER_ID
    }

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
    fun `createUser accepts valid usernames`(username: String) {
        val result = userService.createPendingUser(username, VALID_DISPLAY_NAME, Role.USER)

        assertEquals(CREATED_USER_ID.asSuccess(), result)

        verify(exactly = 1) {
            userRepository.createUser(username, null, VALID_DISPLAY_NAME, Role.USER, ActivationStatus.PENDING)
        }
    }

    @Test
    fun `createUser accepts username at max length`() {
        val username = "a" + "b".repeat(UserService.MAX_USERNAME_LENGTH - 2) + "1"

        val result = userService.createPendingUser(username, VALID_DISPLAY_NAME, Role.USER)

        assertEquals(CREATED_USER_ID.asSuccess(), result)

        verify(exactly = 1) {
            userRepository.createUser(username, null, VALID_DISPLAY_NAME, Role.USER, ActivationStatus.PENDING)
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
    fun `createUser rejects invalid usernames`(username: String) {
        val result = userService.createPendingUser(username, username, Role.USER)

        assertEquals(Failure(CreateUserFailure.INVALID_USERNAME), result)

        verify(exactly = 0) {
            database.transaction<CreateUserResult>(any())
            userRepository.createUser(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `createUser rejects username longer than max length`() {
        val username = "a" + "b".repeat(UserService.MAX_USERNAME_LENGTH - 1) + "1"

        val result = userService.createPendingUser(username, username, Role.USER)

        assertEquals(Failure(CreateUserFailure.INVALID_USERNAME), result)

        verify(exactly = 0) {
            database.transaction<CreateUserResult>(any())
            userRepository.createUser(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `findUserByUsername normalises username before lookup`() {
        every { database.transaction<UserLoginDetail?>(any()) } answers { firstArg<() -> UserLoginDetail?>().invoke() }
        every { userRepository.findUserLoginByUsername("user-one") } returns FOUND_USER

        val result = userService.findUserLoginByUsername("  User-One  ")

        assertEquals(FOUND_USER, result)
        verify(exactly = 1) { userRepository.findUserLoginByUsername("user-one") }
    }

    @Test
    fun `createUser normalises username before saving`() {
        val result = userService.createPendingUser("  User-One  ", VALID_DISPLAY_NAME, Role.USER)

        assertEquals(CREATED_USER_ID.asSuccess(), result)

        verify(exactly = 1) {
            userRepository.createUser("user-one", null, VALID_DISPLAY_NAME, Role.USER, ActivationStatus.PENDING)
        }
    }

//    @Test
//    fun `createUser rejects password shorter than minimum length`() {
//        val result = userService.createPendingUser(
//            username = "valid-user",
//            password = Secret("a".repeat(UserService.MIN_PASSWORD_LENGTH - 1)),
//        )
//
//        assertEquals(Failure(CreateUserFailure.INVALID_PASSWORD), result)
//
//        verify(exactly = 0) {
//            passwordHasherService.hash(any())
//            database.transaction<CreateUserResult>(any())
//            userRepository.createUser(any(), any())
//        }
//    }
//
//    @Test
//    fun `createUser rejects password longer than maximum length`() {
//        val result = userService.createUser(
//            username = "valid-user",
//            password = Secret("a".repeat(UserService.MAX_PASSWORD_LENGTH + 1)),
//        )
//
//        assertEquals(Failure(CreateUserFailure.INVALID_PASSWORD), result)
//
//        verify(exactly = 0) {
//            passwordHasherService.hash(any())
//            database.transaction<CreateUserResult>(any())
//            userRepository.createUser(any(), any(), any(), any(), any())
//        }
//    }

    @Test
    fun `createUser returns existing user failure when insert fails`() {
        every {
            userRepository.createUser("existing-user", null, VALID_DISPLAY_NAME, Role.USER, ActivationStatus.PENDING)
        } throws SQLException("duplicate username")

        val result = userService.createPendingUser("existing-user", VALID_DISPLAY_NAME, Role.USER)

        assertEquals(Failure(CreateUserFailure.EXISTING_USER), result)
    }

    private companion object {
        const val VALID_PASSWORD = "valid-password"
        const val HASHED_PASSWORD = "hashed-password"
        const val VALID_DISPLAY_NAME = "Valid Display Name"

        val CREATED_USER_ID: Uuid = Uuid.random()
        val FOUND_USER = UserLoginDetail(
            id = Uuid.random(),
            username = "user-one",
            passwordHash = Secret(HASHED_PASSWORD),
            role = Role.USER,
            activationStatus = ActivationStatus.PENDING,
            enabled = true,
        )
    }
}

private typealias CreateUserResult = Result4k<Uuid, CreateUserFailure>