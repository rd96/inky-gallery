package uk.derbyshire.domain.auth

import uk.derbyshire.domain.devices.DeviceId
import uk.derbyshire.domain.users.UserId

data class AuthenticatedDevice(
    val userId: UserId,
    val deviceId: DeviceId,
)