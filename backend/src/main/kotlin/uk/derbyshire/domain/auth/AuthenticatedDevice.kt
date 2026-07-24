package uk.derbyshire.domain.auth

import uk.derbyshire.domain.users.UserId
import kotlin.uuid.Uuid

data class AuthenticatedDevice(
    val userId: UserId,
    val deviceId: Uuid,
)