package uk.derbyshire.domain.auth

import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.UserStatus
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class SessionUser(
    val sessionId: Uuid,
    val expiresAt: Instant,
    val userId: Uuid,
    val username: String,
    val role: Role,
    val status: UserStatus,
)