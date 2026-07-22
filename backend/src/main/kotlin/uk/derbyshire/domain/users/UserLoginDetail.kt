package uk.derbyshire.domain.users

import org.http4k.config.Secret

data class UserLoginDetail(
    val id: UserId,
    val username: String,
    val passwordHash: Secret?,
    val role: Role,
    val activationStatus: ActivationStatus,
    val enabled: Boolean,
)