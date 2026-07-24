package uk.derbyshire.domain.auth

import uk.derbyshire.domain.users.UserId
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class ApiKeyUser(
    val userId: UserId,
    val userEnabled: Boolean,
    val deviceId: Uuid,
    val deviceEnabled: Boolean,
    val revokedAt: Instant?,
)