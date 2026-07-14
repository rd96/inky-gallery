package uk.derbyshire.auth

import kotlin.uuid.Uuid

data class AuthenticatedUser(
    val userId: Uuid,
    val username: String,
    val role: Role,
)