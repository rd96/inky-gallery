package uk.derbyshire.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.HexFormat

class SessionTokens(
    private val random: SecureRandom = SecureRandom(),
) {
    private val hex = HexFormat.of()

    fun generate(): String {
        val bytes = ByteArray(32) // 256 bits
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun hash(token: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(token.toByteArray(Charsets.UTF_8))

        return hex.formatHex(bytes)
    }
}