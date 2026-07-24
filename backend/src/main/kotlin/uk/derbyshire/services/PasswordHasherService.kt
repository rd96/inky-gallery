package uk.derbyshire.services

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory

class PasswordHasherService {
    private val argon2: Argon2 = Argon2Factory.create(
        Argon2Factory.Argon2Types.ARGON2id,
    )

    fun validateAndHashPassword(password: String): String? =
        if (validPassword(password)) hash(password)
        else null

    private fun hash(password: String): String =
        argon2.hash(
            3,          // iterations
            64 * 1024,  // memory in KiB = 64 MiB
            1,          // parallelism
            password.toCharArray(),
        )

    fun verify(password: String, passwordHash: String): Boolean =
        argon2.verify(
            passwordHash,
            password.toCharArray(),
        )

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 100

        private fun validPassword(password: String) =
            password.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH
    }
}