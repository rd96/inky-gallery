package uk.derbyshire.domain.connections

import uk.derbyshire.domain.users.UserId
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class UserConnection(
    val connectionId: Uuid,
    val userId: UserId,
    val username: String,
    val displayName: String,
    val createdAt: Instant,
    val enabled: Boolean,
)