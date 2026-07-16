package uk.derbyshire.domain.users

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class UserSummary(
    val id: Uuid,
    val username: String,
    val displayName: String,
    val role: Role,
    val status: UserStatus,
    val createdAt: Instant,
)