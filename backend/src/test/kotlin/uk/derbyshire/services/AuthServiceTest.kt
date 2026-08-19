package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.asSuccess
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.http4k.config.Secret
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.AccountTokenRepository
import uk.derbyshire.database.repositories.DeviceApiKeyRepository
import uk.derbyshire.database.repositories.SessionRepository
import uk.derbyshire.database.repositories.UserRepository
import uk.derbyshire.domain.auth.AccountTokenType
import uk.derbyshire.domain.auth.ActivationFailure
import uk.derbyshire.domain.auth.ActivationToken
import uk.derbyshire.domain.auth.ApiKeyUser
import uk.derbyshire.domain.auth.AuthenticatedDevice
import uk.derbyshire.domain.auth.AuthenticatedUser
import uk.derbyshire.domain.auth.LoginFailure
import uk.derbyshire.domain.auth.LoginSuccess
import uk.derbyshire.domain.auth.SessionId
import uk.derbyshire.domain.auth.SessionUser
import uk.derbyshire.domain.auth.UserActivationToken
import uk.derbyshire.domain.auth.UserPendingActivation
import uk.derbyshire.domain.devices.DeviceId
import uk.derbyshire.domain.users.ActivationStatus
import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.UserId
import uk.derbyshire.domain.users.UserLoginDetail
import uk.derbyshire.domain.users.UserSummary
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AuthServiceTest {
    private val sessionRepository = mockk<SessionRepository>()
    private val accountTokenRepository = mockk<AccountTokenRepository>()
    private val apiKeyRepository = mockk<DeviceApiKeyRepository>()
    private val userRepository = mockk<UserRepository>()
    private val secureTokenService = mockk<SecureTokenService>()
    private val passwordHasherService = mockk<PasswordHasherService>()
    private val context = mockk<DatabaseContext>()
    private val clock = mockk<Clock>()

    private val authService = AuthService(
        sessionRepository = sessionRepository,
        accountTokenRepository = accountTokenRepository,
        apiKeyRepository = apiKeyRepository,
        userRepository = userRepository,
        secureTokenService = secureTokenService,
        passwordHasherService = passwordHasherService,
        context = context,
        clock = clock,
    )

    @BeforeEach
    fun setUp() {
        clearMocks(
            sessionRepository,
            accountTokenRepository,
            apiKeyRepository,
            userRepository,
            secureTokenService,
            passwordHasherService,
            context,
            clock,
        )

        every {
            context.transaction<Any?>(any())
        } answers {
            firstArg<() -> Any?>().invoke()
        }

        every { clock.now() } returns NOW
    }

    @Nested
    inner class Login {
        @Test
        fun `fails when user not found`() {
            every { userRepository.findUserLoginByUsername(USERNAME) } returns null

            val result = authService.login(USERNAME, Secret(PASSWORD))

            assertEquals(Failure(LoginFailure.USER_NOT_FOUND), result)
            verify(exactly = 0) { passwordHasherService.verify(any(), any()) }
        }

        @Test
        fun `fails when user is pending activation`() {
            every { userRepository.findUserLoginByUsername(USERNAME) } returns pendingUser()

            val result = authService.login(USERNAME, Secret(PASSWORD))

            assertEquals(Failure(LoginFailure.USER_PENDING_ACTIVATION), result)
            verify(exactly = 0) { passwordHasherService.verify(any(), any()) }
        }

        @Test
        fun `fails when an activated user has no password hash`() {
            every {
                userRepository.findUserLoginByUsername(USERNAME)
            } returns activatedUser().copy(passwordHash = null)

            val result = authService.login(USERNAME, Secret(PASSWORD))

            assertEquals(Failure(LoginFailure.USER_PENDING_ACTIVATION), result)
        }

        @Test
        fun `fails when user is disabled`() {
            every {
                userRepository.findUserLoginByUsername(USERNAME)
            } returns activatedUser().copy(enabled = false)

            val result = authService.login(USERNAME, Secret(PASSWORD))

            assertEquals(Failure(LoginFailure.USER_DISABLED), result)
            verify(exactly = 0) { passwordHasherService.verify(any(), any()) }
        }

        @Test
        fun `reports pending activation for a disabled pending user`() {
            every {
                userRepository.findUserLoginByUsername(USERNAME)
            } returns pendingUser().copy(enabled = false)

            val result = authService.login(USERNAME, Secret(PASSWORD))

            assertEquals(Failure(LoginFailure.USER_PENDING_ACTIVATION), result)
        }

        @Test
        fun `fails when password is incorrect`() {
            every { userRepository.findUserLoginByUsername(USERNAME) } returns activatedUser()
            every { passwordHasherService.verify(PASSWORD, PASSWORD_HASH) } returns false

            val result = authService.login(USERNAME, Secret(PASSWORD))

            assertEquals(Failure(LoginFailure.PASSWORD_INCORRECT), result)
            verify(exactly = 0) { sessionRepository.createSession(any(), any(), any()) }
        }

        @Test
        fun `creates a session and returns success when password matches`() {
            every { userRepository.findUserLoginByUsername(USERNAME) } returns activatedUser()
            every { passwordHasherService.verify(PASSWORD, PASSWORD_HASH) } returns true
            every { secureTokenService.generate() } returns RAW_TOKEN
            every { secureTokenService.hash(RAW_TOKEN) } returns TOKEN_HASH
            every {
                sessionRepository.createSession(CREATED_USER_ID, TOKEN_HASH, NOW + AuthService.SESSION_EXPIRES_AFTER)
            } just Runs

            val result = authService.login(USERNAME, Secret(PASSWORD))

            assertEquals(LoginSuccess(Secret(RAW_TOKEN)).asSuccess(), result)
            verify(exactly = 1) {
                sessionRepository.createSession(CREATED_USER_ID, TOKEN_HASH, NOW + AuthService.SESSION_EXPIRES_AFTER)
            }
        }
    }

    @Nested
    inner class AuthenticateSession {
        @Test
        fun `returns null when session not found`() {
            every { secureTokenService.hash(RAW_TOKEN) } returns TOKEN_HASH
            every { sessionRepository.findUserBySessionTokenHash(TOKEN_HASH) } returns null

            val result = authService.authenticateSession(RAW_TOKEN)

            assertNull(result)
        }

        @Test
        fun `returns null when user is not activated`() {
            every { secureTokenService.hash(RAW_TOKEN) } returns TOKEN_HASH
            every {
                sessionRepository.findUserBySessionTokenHash(TOKEN_HASH)
            } returns VALID_SESSION.copy(activationStatus = ActivationStatus.PENDING)

            val result = authService.authenticateSession(RAW_TOKEN)

            assertNull(result)
        }

        @Test
        fun `returns null when user is disabled`() {
            every { secureTokenService.hash(RAW_TOKEN) } returns TOKEN_HASH
            every {
                sessionRepository.findUserBySessionTokenHash(TOKEN_HASH)
            } returns VALID_SESSION.copy(enabled = false)

            val result = authService.authenticateSession(RAW_TOKEN)

            assertNull(result)
        }

        @Test
        fun `deletes and returns null for an expired session`() {
            every { secureTokenService.hash(RAW_TOKEN) } returns TOKEN_HASH
            every {
                sessionRepository.findUserBySessionTokenHash(TOKEN_HASH)
            } returns VALID_SESSION.copy(expiresAt = NOW - 1.minutes)
            every { sessionRepository.deleteSession(VALID_SESSION.sessionId) } just Runs

            val result = authService.authenticateSession(RAW_TOKEN)

            assertNull(result)
            verify(exactly = 1) { sessionRepository.deleteSession(VALID_SESSION.sessionId) }
        }

        @Test
        fun `touches last seen when stale and returns the authenticated user`() {
            every { secureTokenService.hash(RAW_TOKEN) } returns TOKEN_HASH
            every {
                sessionRepository.findUserBySessionTokenHash(TOKEN_HASH)
            } returns VALID_SESSION.copy(lastSeen = NOW - 2.minutes)
            every { sessionRepository.updateLastSeen(VALID_SESSION.sessionId, NOW) } just Runs

            val result = authService.authenticateSession(RAW_TOKEN)

            assertEquals(
                AuthenticatedUser(
                    VALID_SESSION.userId,
                    VALID_SESSION.username,
                    VALID_SESSION.role,
                    VALID_SESSION.displayName,
                ),
                result,
            )
            verify(exactly = 1) { sessionRepository.updateLastSeen(VALID_SESSION.sessionId, NOW) }
        }

        @Test
        fun `does not touch last seen when recently seen`() {
            every { secureTokenService.hash(RAW_TOKEN) } returns TOKEN_HASH
            every {
                sessionRepository.findUserBySessionTokenHash(TOKEN_HASH)
            } returns VALID_SESSION.copy(lastSeen = NOW)

            val result = authService.authenticateSession(RAW_TOKEN)

            assertNotNull(result)
            verify(exactly = 0) { sessionRepository.updateLastSeen(any(), any()) }
        }
    }

    @Nested
    inner class AuthenticateApiKey {
        @Test
        fun `returns null when key not found`() {
            every { secureTokenService.hash(RAW_API_KEY) } returns API_KEY_HASH
            every { apiKeyRepository.findUserByApiKeyHash(API_KEY_HASH) } returns null

            val result = authService.authenticateApiKey(RAW_API_KEY)

            assertNull(result)
        }

        @Test
        fun `returns null when key is revoked`() {
            every { secureTokenService.hash(RAW_API_KEY) } returns API_KEY_HASH
            every {
                apiKeyRepository.findUserByApiKeyHash(API_KEY_HASH)
            } returns VALID_API_KEY_USER.copy(revokedAt = NOW)

            val result = authService.authenticateApiKey(RAW_API_KEY)

            assertNull(result)
        }

        @Test
        fun `returns null when user is disabled`() {
            every { secureTokenService.hash(RAW_API_KEY) } returns API_KEY_HASH
            every {
                apiKeyRepository.findUserByApiKeyHash(API_KEY_HASH)
            } returns VALID_API_KEY_USER.copy(userEnabled = false)

            val result = authService.authenticateApiKey(RAW_API_KEY)

            assertNull(result)
        }

        @Test
        fun `returns null when device is disabled`() {
            every { secureTokenService.hash(RAW_API_KEY) } returns API_KEY_HASH
            every {
                apiKeyRepository.findUserByApiKeyHash(API_KEY_HASH)
            } returns VALID_API_KEY_USER.copy(deviceEnabled = false)

            val result = authService.authenticateApiKey(RAW_API_KEY)

            assertNull(result)
        }

        @Test
        fun `returns the authenticated device when the key is valid`() {
            every { secureTokenService.hash(RAW_API_KEY) } returns API_KEY_HASH
            every { apiKeyRepository.findUserByApiKeyHash(API_KEY_HASH) } returns VALID_API_KEY_USER

            val result = authService.authenticateApiKey(RAW_API_KEY)

            assertEquals(
                AuthenticatedDevice(VALID_API_KEY_USER.userId, VALID_API_KEY_USER.deviceId),
                result,
            )
        }
    }

    @Nested
    inner class CreateSession {
        @Test
        fun `stores the token hash and returns the raw token`() {
            every { secureTokenService.generate() } returns RAW_TOKEN
            every { secureTokenService.hash(RAW_TOKEN) } returns TOKEN_HASH
            every {
                sessionRepository.createSession(CREATED_USER_ID, TOKEN_HASH, NOW + AuthService.SESSION_EXPIRES_AFTER)
            } just Runs

            val result = authService.createSession(CREATED_USER_ID)

            assertEquals(Secret(RAW_TOKEN), result)
            verify(exactly = 1) {
                sessionRepository.createSession(CREATED_USER_ID, TOKEN_HASH, NOW + AuthService.SESSION_EXPIRES_AFTER)
            }
        }
    }

    @Nested
    inner class ActivateUser {
        @Test
        fun `rejects an invalid password without touching the activation token repository`() {
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every { passwordHasherService.validateAndHashPassword(any()) } returns null

            val result = authService.activateUser(Secret(RAW_ACTIVATION_TOKEN), Secret("short"))

            assertEquals(Failure(ActivationFailure.PASSWORD_INVALID), result)
            verify(exactly = 0) { accountTokenRepository.getActivationTokenByHash(any()) }
        }

        @Test
        fun `fails when the token is unknown`() {
            every { passwordHasherService.validateAndHashPassword(VALID_PASSWORD) } returns HASHED_PASSWORD
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every { accountTokenRepository.getActivationTokenByHash(ACTIVATION_TOKEN_HASH) } returns null

            val result = authService.activateUser(Secret(RAW_ACTIVATION_TOKEN), Secret(VALID_PASSWORD))

            assertEquals(Failure(ActivationFailure.INVALID_ACTIVATION_TOKEN), result)
        }

        @Test
        fun `fails when the token has expired`() {
            every { passwordHasherService.validateAndHashPassword(VALID_PASSWORD) } returns HASHED_PASSWORD
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every {
                accountTokenRepository.getActivationTokenByHash(ACTIVATION_TOKEN_HASH)
            } returns VALID_ACTIVATION_TOKEN.copy(expiresAt = NOW)

            val result = authService.activateUser(Secret(RAW_ACTIVATION_TOKEN), Secret(VALID_PASSWORD))

            assertEquals(Failure(ActivationFailure.INVALID_ACTIVATION_TOKEN), result)
        }

        @Test
        fun `fails when the token was already used`() {
            every { passwordHasherService.validateAndHashPassword(VALID_PASSWORD) } returns HASHED_PASSWORD
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every {
                accountTokenRepository.getActivationTokenByHash(ACTIVATION_TOKEN_HASH)
            } returns VALID_ACTIVATION_TOKEN.copy(usedAt = NOW)

            val result = authService.activateUser(Secret(RAW_ACTIVATION_TOKEN), Secret(VALID_PASSWORD))

            assertEquals(Failure(ActivationFailure.INVALID_ACTIVATION_TOKEN), result)
        }

        @Test
        fun `fails when the token was revoked`() {
            every { passwordHasherService.validateAndHashPassword(VALID_PASSWORD) } returns HASHED_PASSWORD
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every {
                accountTokenRepository.getActivationTokenByHash(ACTIVATION_TOKEN_HASH)
            } returns VALID_ACTIVATION_TOKEN.copy(revokedAt = NOW)

            val result = authService.activateUser(Secret(RAW_ACTIVATION_TOKEN), Secret(VALID_PASSWORD))

            assertEquals(Failure(ActivationFailure.INVALID_ACTIVATION_TOKEN), result)
        }

        @Test
        fun `fails when the token's user no longer exists`() {
            every { passwordHasherService.validateAndHashPassword(VALID_PASSWORD) } returns HASHED_PASSWORD
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every {
                accountTokenRepository.getActivationTokenByHash(ACTIVATION_TOKEN_HASH)
            } returns VALID_ACTIVATION_TOKEN
            every { userRepository.findUser(CREATED_USER_ID) } returns null

            val result = authService.activateUser(Secret(RAW_ACTIVATION_TOKEN), Secret(VALID_PASSWORD))

            assertEquals(Failure(ActivationFailure.PENDING_USER_NOT_FOUND), result)
        }

        @Test
        fun `fails when the user is already activated`() {
            every { passwordHasherService.validateAndHashPassword(VALID_PASSWORD) } returns HASHED_PASSWORD
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every {
                accountTokenRepository.getActivationTokenByHash(ACTIVATION_TOKEN_HASH)
            } returns VALID_ACTIVATION_TOKEN
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns PENDING_USER_SUMMARY.copy(activationStatus = ActivationStatus.ACTIVATED)

            val result = authService.activateUser(Secret(RAW_ACTIVATION_TOKEN), Secret(VALID_PASSWORD))

            assertEquals(Failure(ActivationFailure.USER_ALREADY_ACTIVATED), result)
        }

        @Test
        fun `fails when the user is disabled`() {
            every { passwordHasherService.validateAndHashPassword(VALID_PASSWORD) } returns HASHED_PASSWORD
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every {
                accountTokenRepository.getActivationTokenByHash(ACTIVATION_TOKEN_HASH)
            } returns VALID_ACTIVATION_TOKEN
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns PENDING_USER_SUMMARY.copy(enabled = false)

            val result = authService.activateUser(Secret(RAW_ACTIVATION_TOKEN), Secret(VALID_PASSWORD))

            assertEquals(Failure(ActivationFailure.USER_DISABLED), result)
        }

        @Test
        fun `propagates a repository failure when setting the password`() {
            every { passwordHasherService.validateAndHashPassword(VALID_PASSWORD) } returns HASHED_PASSWORD
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every {
                accountTokenRepository.getActivationTokenByHash(ACTIVATION_TOKEN_HASH)
            } returns VALID_ACTIVATION_TOKEN
            every { userRepository.findUser(CREATED_USER_ID) } returns PENDING_USER_SUMMARY
            every {
                userRepository.setPendingUserPasswordAndStatusActivated(CREATED_USER_ID, HASHED_PASSWORD)
            } returns Failure(ActivationFailure.PENDING_USER_NOT_FOUND)

            val result = authService.activateUser(Secret(RAW_ACTIVATION_TOKEN), Secret(VALID_PASSWORD))

            assertEquals(Failure(ActivationFailure.PENDING_USER_NOT_FOUND), result)
            verify(exactly = 0) { sessionRepository.createSession(any(), any(), any()) }
        }

        @Test
        fun `activates the user and returns a new session on success`() {
            every { passwordHasherService.validateAndHashPassword(VALID_PASSWORD) } returns HASHED_PASSWORD
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every {
                accountTokenRepository.getActivationTokenByHash(ACTIVATION_TOKEN_HASH)
            } returns VALID_ACTIVATION_TOKEN
            every { userRepository.findUser(CREATED_USER_ID) } returns PENDING_USER_SUMMARY
            every {
                userRepository.setPendingUserPasswordAndStatusActivated(CREATED_USER_ID, HASHED_PASSWORD)
            } returns Unit.asSuccess()
            every { secureTokenService.generate() } returns RAW_TOKEN
            every { secureTokenService.hash(RAW_TOKEN) } returns TOKEN_HASH
            every {
                sessionRepository.createSession(CREATED_USER_ID, TOKEN_HASH, NOW + AuthService.SESSION_EXPIRES_AFTER)
            } just Runs

            val result = authService.activateUser(Secret(RAW_ACTIVATION_TOKEN), Secret(VALID_PASSWORD))

            assertEquals(LoginSuccess(Secret(RAW_TOKEN)).asSuccess(), result)
        }
    }

    @Nested
    inner class GetActivationDetails {
        @Test
        fun `fails when the token is invalid`() {
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every { accountTokenRepository.getActivationTokenByHash(ACTIVATION_TOKEN_HASH) } returns null

            val result = authService.getActivationDetails(Secret(RAW_ACTIVATION_TOKEN))

            assertEquals(Failure(ActivationFailure.INVALID_ACTIVATION_TOKEN), result)
        }

        @Test
        fun `returns the pending user's details for a valid token`() {
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every {
                accountTokenRepository.getActivationTokenByHash(ACTIVATION_TOKEN_HASH)
            } returns VALID_ACTIVATION_TOKEN
            every { userRepository.findUser(CREATED_USER_ID) } returns PENDING_USER_SUMMARY

            val result = authService.getActivationDetails(Secret(RAW_ACTIVATION_TOKEN))

            assertEquals(
                UserActivationToken(
                    userId = CREATED_USER_ID,
                    username = PENDING_USER_SUMMARY.username,
                    displayName = PENDING_USER_SUMMARY.displayName,
                    expiresAt = VALID_ACTIVATION_TOKEN.expiresAt,
                ).asSuccess(),
                result,
            )
        }
    }

    @Nested
    inner class GenerateUserActivationToken {
        @Test
        fun `fails when the user does not exist`() {
            every { userRepository.findUser(CREATED_USER_ID) } returns null

            val result = authService.generateUserActivationToken(CREATED_USER_ID, ADMIN_USER_ID)

            assertEquals(Failure("User $CREATED_USER_ID not found"), result)
        }

        @Test
        fun `fails when the user is already activated`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns PENDING_USER_SUMMARY.copy(activationStatus = ActivationStatus.ACTIVATED)

            val result = authService.generateUserActivationToken(CREATED_USER_ID, ADMIN_USER_ID)

            assertEquals(Failure("User $CREATED_USER_ID is already activated"), result)
        }

        @Test
        fun `fails when the user is disabled`() {
            every {
                userRepository.findUser(CREATED_USER_ID)
            } returns PENDING_USER_SUMMARY.copy(enabled = false)

            val result = authService.generateUserActivationToken(CREATED_USER_ID, ADMIN_USER_ID)

            assertEquals(Failure("User $CREATED_USER_ID is not enabled"), result)
        }

        @Test
        fun `revokes existing tokens and creates a new one`() {
            every { userRepository.findUser(CREATED_USER_ID) } returns PENDING_USER_SUMMARY
            every { secureTokenService.generate() } returns RAW_ACTIVATION_TOKEN
            every { secureTokenService.hash(RAW_ACTIVATION_TOKEN) } returns ACTIVATION_TOKEN_HASH
            every {
                accountTokenRepository.revokeTokensForUser(CREATED_USER_ID, AccountTokenType.ACTIVATION,NOW)
            } just Runs
            every {
                accountTokenRepository.createAccountToken(
                    CREATED_USER_ID,
                    AccountTokenType.ACTIVATION,
                    ACTIVATION_TOKEN_HASH,
                    NOW + AuthService.ACTIVATION_TOKEN_EXPIRES_AFTER,
                    ADMIN_USER_ID,
                    NOW,
                )
            } just Runs

            val result = authService.generateUserActivationToken(CREATED_USER_ID, ADMIN_USER_ID)

            assertEquals(
                UserPendingActivation(
                    userId = CREATED_USER_ID,
                    activationToken = RAW_ACTIVATION_TOKEN,
                    expiresAt = NOW + AuthService.ACTIVATION_TOKEN_EXPIRES_AFTER,
                ).asSuccess(),
                result,
            )
            verify(exactly = 1) {
                accountTokenRepository.revokeTokensForUser(CREATED_USER_ID, AccountTokenType.ACTIVATION, NOW)
            }
        }
    }

    @Nested
    inner class Logout {
        @Test
        fun `deletes the session for the hashed token`() {
            every { secureTokenService.hash(RAW_TOKEN) } returns TOKEN_HASH
            every { sessionRepository.deleteByTokenHash(TOKEN_HASH) } just Runs

            authService.logout(RAW_TOKEN)

            verify(exactly = 1) { sessionRepository.deleteByTokenHash(TOKEN_HASH) }
        }
    }

    @Nested
    inner class RevokeUserActivationTokens {
        @Test
        fun `fails when the user does not exist`() {
            every { userRepository.userExists(CREATED_USER_ID) } returns false

            val result = authService.revokeUserActivationTokens(CREATED_USER_ID)

            assertEquals(Failure("User $CREATED_USER_ID not found"), result)
            verify(exactly = 0) { accountTokenRepository.revokeTokensForUser(any(), any(), any()) }
        }

        @Test
        fun `revokes tokens when the user exists`() {
            every { userRepository.userExists(CREATED_USER_ID) } returns true
            every { accountTokenRepository.revokeTokensForUser(CREATED_USER_ID, AccountTokenType.ACTIVATION, NOW) } just Runs

            val result = authService.revokeUserActivationTokens(CREATED_USER_ID)

            assertEquals(Unit.asSuccess(), result)
            verify(exactly = 1) { accountTokenRepository.revokeTokensForUser(CREATED_USER_ID, AccountTokenType.ACTIVATION, NOW) }
        }
    }

    @Nested
    inner class DisableUserAndRevokeSessions {
        @Test
        fun `disables the user and deletes their sessions`() {
            every { userRepository.disableUser(CREATED_USER_ID) } just Runs
            every { sessionRepository.deleteSessionsForUser(CREATED_USER_ID) } just Runs

            authService.disableUserAndRevokeSessions(CREATED_USER_ID)

            verify(exactly = 1) { userRepository.disableUser(CREATED_USER_ID) }
            verify(exactly = 1) { sessionRepository.deleteSessionsForUser(CREATED_USER_ID) }
        }
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-01-01T00:00:00Z")

        const val USERNAME = "user-one"
        const val PASSWORD = "correct-password"
        const val PASSWORD_HASH = "stored-password-hash"

        const val RAW_TOKEN = "raw-session-token"
        const val TOKEN_HASH = "session-token-hash"

        const val RAW_API_KEY = "raw-api-key"
        const val API_KEY_HASH = "api-key-hash"

        const val VALID_PASSWORD = "valid-password"
        const val HASHED_PASSWORD = "hashed-password"

        const val RAW_ACTIVATION_TOKEN = "raw-activation-token"
        const val ACTIVATION_TOKEN_HASH = "activation-token-hash"

        val CREATED_USER_ID = UserId(Uuid.parse("00000000-0000-0000-0000-000000000001"))
        val ADMIN_USER_ID = UserId(Uuid.parse("00000000-0000-0000-0000-000000000002"))
        val DEVICE_ID = DeviceId(Uuid.parse("00000000-0000-0000-0000-000000000003"))
        val SESSION_ID = SessionId(Uuid.parse("00000000-0000-0000-0000-000000000004"))

        fun activatedUser() = UserLoginDetail(
            id = CREATED_USER_ID,
            username = USERNAME,
            passwordHash = Secret(PASSWORD_HASH),
            role = Role.USER,
            activationStatus = ActivationStatus.ACTIVATED,
            enabled = true,
        )

        fun pendingUser() = activatedUser().copy(
            passwordHash = null,
            activationStatus = ActivationStatus.PENDING,
        )

        val VALID_SESSION = SessionUser(
            sessionId = SESSION_ID,
            expiresAt = NOW + 1.days,
            lastSeen = NOW,
            userId = CREATED_USER_ID,
            username = USERNAME,
            role = Role.USER,
            displayName = "User One",
            activationStatus = ActivationStatus.ACTIVATED,
            enabled = true,
        )

        val VALID_API_KEY_USER = ApiKeyUser(
            userId = CREATED_USER_ID,
            userEnabled = true,
            deviceId = DEVICE_ID,
            deviceEnabled = true,
            revokedAt = null,
        )

        val VALID_ACTIVATION_TOKEN = ActivationToken(
            userId = CREATED_USER_ID,
            expiresAt = NOW + 1.days,
            usedAt = null,
            revokedAt = null,
        )

        val PENDING_USER_SUMMARY = UserSummary(
            id = CREATED_USER_ID,
            username = USERNAME,
            displayName = "User One",
            role = Role.USER,
            activationStatus = ActivationStatus.PENDING,
            enabled = true,
            createdAt = NOW,
        )
    }
}
