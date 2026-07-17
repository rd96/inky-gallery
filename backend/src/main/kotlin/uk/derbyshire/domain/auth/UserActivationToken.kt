package uk.derbyshire.domain.auth

import kotlin.time.Instant

data class UserActivationToken(
    val username: String,
    val displayName: String,
    val expiresAt: Instant,
)