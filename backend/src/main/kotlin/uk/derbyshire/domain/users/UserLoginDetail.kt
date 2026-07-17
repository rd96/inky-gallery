package uk.derbyshire.domain.users

import org.http4k.config.Secret
import kotlin.uuid.Uuid

data class UserLoginDetail(
    val id: Uuid,
    val username: String,
    val passwordHash: Secret?,
    val role: Role,
    val activationStatus: ActivationStatus,
    val enabled: Boolean,
)