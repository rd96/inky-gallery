package uk.derbyshire.domain.auth

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class UserPendingActivation(
    val userId: Uuid,
    val activationToken: String,
    val expiresAt: Instant,
)