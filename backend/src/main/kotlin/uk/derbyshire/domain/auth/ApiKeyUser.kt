package uk.derbyshire.domain.auth

import uk.derbyshire.domain.devices.DeviceId
import uk.derbyshire.domain.users.UserId
import kotlin.time.Instant

data class ApiKeyUser(
    val userId: UserId,
    val userEnabled: Boolean,
    val deviceId: DeviceId,
    val deviceEnabled: Boolean,
    val revokedAt: Instant?,
)