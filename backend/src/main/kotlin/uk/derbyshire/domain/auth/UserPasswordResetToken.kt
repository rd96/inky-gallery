package uk.derbyshire.domain.auth

import uk.derbyshire.domain.users.UserId
import kotlin.time.Instant

data class UserPasswordResetToken(
    val userId: UserId,
    val username: String,
    val displayName: String,
    val expiresAt: Instant,
)
