package uk.derbyshire.domain.auth

import kotlin.time.Instant

data class PasswordResetToken(
    val resetToken: String,
    val expiresAt: Instant,
)