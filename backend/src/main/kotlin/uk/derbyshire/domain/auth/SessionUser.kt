package uk.derbyshire.domain.auth

import uk.derbyshire.domain.users.ActivationStatus
import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.UserId
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class SessionUser(
    val sessionId: Uuid,
    val expiresAt: Instant,
    val userId: UserId,
    val username: String,
    val role: Role,
    val displayName: String,
    val activationStatus: ActivationStatus,
    val enabled: Boolean,
)