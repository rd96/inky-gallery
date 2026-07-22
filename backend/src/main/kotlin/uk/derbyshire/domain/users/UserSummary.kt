package uk.derbyshire.domain.users

import kotlin.time.Instant

data class UserSummary(
    val id: UserId,
    val username: String,
    val displayName: String,
    val role: Role,
    val activationStatus: ActivationStatus,
    val enabled: Boolean,
    val createdAt: Instant,
)