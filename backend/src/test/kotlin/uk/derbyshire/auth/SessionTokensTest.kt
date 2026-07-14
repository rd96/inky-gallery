package uk.derbyshire.auth

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import java.security.SecureRandom
import kotlin.test.Test

class SessionTokensTest {
    @Test
    fun `generate creates a 256-bit URL-safe token without padding`() {
        val random = mockk<SecureRandom>()
        val generatedBytes = ByteArray(32) { it.toByte() }
        every { random.nextBytes(any()) } answers {
            generatedBytes.copyInto(firstArg<ByteArray>())
        }

        val token = SessionTokens(random).generate()

        assertEquals(
            "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8",
            token,
        )
        assertEquals(43, token.length)
        assertTrue(token.none { it == '=' })
        assertTrue(token.all { it.isLetterOrDigit() || it == '-' || it == '_' })
    }

    @Test
    fun `hash returns the SHA-256 digest as lowercase hexadecimal`() {
        val hash = SessionTokens().hash("test-token")

        assertEquals(
            "4c5dc9b7708905f77f5e5d16316b5dfb425e68cb326dcd55a860e90a7707031e",
            hash,
        )
        assertEquals(64, hash.length)
        assertTrue(hash.all { it in '0'..'9' || it in 'a'..'f' })
    }

    @Test
    fun `hash encodes the token using UTF-8`() {
        val hash = SessionTokens().hash("pässwörd")

        assertEquals(
            "46970bef70aced8123f0d5d094717e2a5cd412041e03b26376049fe65b2834a4",
            hash,
        )
    }
}