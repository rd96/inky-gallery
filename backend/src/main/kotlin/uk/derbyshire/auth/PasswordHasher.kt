package uk.derbyshire.auth

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory

class PasswordHasher {
    private val argon2: Argon2 = Argon2Factory.create(
        Argon2Factory.Argon2Types.ARGON2id,
    )

    fun hash(password: String): String =
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
}