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
import uk.derbyshire.auth.PasswordHasher
import uk.derbyshire.auth.Role
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.UserRepository
import java.sql.SQLException
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val database = mockk<DatabaseContext>()

    private val userService = UserService(
        userRepository = userRepository,
        passwordHasher = passwordHasher,
        database = database,
    )

    @BeforeEach
    fun setUp() {
        clearMocks(userRepository, passwordHasher, database)

        every { passwordHasher.hash(any()) } returns HASHED_PASSWORD

        every {
            database.transaction<CreateUserResult>(any())
        } answers {
            firstArg<() -> CreateUserResult>().invoke()
        }

        every {
            userRepository.createUser(any(), HASHED_PASSWORD)
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
        val result = userService.createUser(username, Secret(VALID_PASSWORD))

        assertEquals(CREATED_USER_ID.asSuccess(), result)

        verify(exactly = 1) {
            passwordHasher.hash(VALID_PASSWORD)
            userRepository.createUser(username, HASHED_PASSWORD)
        }
    }

    @Test
    fun `createUser accepts username at max length`() {
        val username = "a" + "b".repeat(UserService.MAX_USERNAME_LENGTH - 2) + "1"

        val result = userService.createUser(username, Secret(VALID_PASSWORD))

        assertEquals(CREATED_USER_ID.asSuccess(), result)

        verify(exactly = 1) {
            userRepository.createUser(username, HASHED_PASSWORD)
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
        val result = userService.createUser(username, Secret(VALID_PASSWORD))

        assertEquals(Failure(CreateUserFailure.INVALID_USERNAME), result)

        verify(exactly = 1) {
            passwordHasher.hash(VALID_PASSWORD)
        }

        verify(exactly = 0) {
            database.transaction<CreateUserResult>(any())
            userRepository.createUser(any(), any())
        }
    }

    @Test
    fun `createUser rejects username longer than max length`() {
        val username = "a" + "b".repeat(UserService.MAX_USERNAME_LENGTH - 1) + "1"

        val result = userService.createUser(username, Secret(VALID_PASSWORD))

        assertEquals(Failure(CreateUserFailure.INVALID_USERNAME), result)

        verify(exactly = 0) {
            database.transaction<CreateUserResult>(any())
            userRepository.createUser(any(), any())
        }
    }

    @Test
    fun `findUserByUsername normalises username before lookup`() {
        every { database.transaction<User?>(any()) } answers { firstArg<() -> User?>().invoke() }
        every { userRepository.findUserByUsername("user-one") } returns FOUND_USER

        val result = userService.findUserByUsername("  User-One  ")

        assertEquals(FOUND_USER, result)
        verify(exactly = 1) { userRepository.findUserByUsername("user-one") }
    }

    @Test
    fun `createUser normalises username before saving`() {
        val result = userService.createUser("  User-One  ", Secret(VALID_PASSWORD))

        assertEquals(CREATED_USER_ID.asSuccess(), result)

        verify(exactly = 1) {
            userRepository.createUser("user-one", HASHED_PASSWORD)
        }
    }

    @Test
    fun `createUser rejects password shorter than minimum length`() {
        val result = userService.createUser(
            username = "valid-user",
            password = Secret("a".repeat(UserService.MIN_PASSWORD_LENGTH - 1)),
        )

        assertEquals(Failure(CreateUserFailure.INVALID_PASSWORD), result)

        verify(exactly = 0) {
            passwordHasher.hash(any())
            database.transaction<CreateUserResult>(any())
            userRepository.createUser(any(), any())
        }
    }

    @Test
    fun `createUser rejects password longer than maximum length`() {
        val result = userService.createUser(
            username = "valid-user",
            password = Secret("a".repeat(UserService.MAX_PASSWORD_LENGTH + 1)),
        )

        assertEquals(Failure(CreateUserFailure.INVALID_PASSWORD), result)

        verify(exactly = 0) {
            passwordHasher.hash(any())
            database.transaction<CreateUserResult>(any())
            userRepository.createUser(any(), any())
        }
    }

    @Test
    fun `createUser returns existing user failure when insert fails`() {
        every {
            userRepository.createUser("existing-user", HASHED_PASSWORD)
        } throws SQLException("duplicate username")

        val result = userService.createUser("existing-user", Secret(VALID_PASSWORD))

        assertEquals(Failure(CreateUserFailure.EXISTING_USER), result)
    }

    private companion object {
        const val VALID_PASSWORD = "valid-password"
        const val HASHED_PASSWORD = "hashed-password"

        val CREATED_USER_ID: Uuid = Uuid.random()
        val FOUND_USER = User(
            id = Uuid.random(),
            username = "user-one",
            passwordHash = Secret(HASHED_PASSWORD),
            role = Role.USER,
            createdAt = Instant.fromEpochMilliseconds(0),
            disabled = false,
        )
    }
}

private typealias CreateUserResult = Result4k<Uuid, CreateUserFailure>