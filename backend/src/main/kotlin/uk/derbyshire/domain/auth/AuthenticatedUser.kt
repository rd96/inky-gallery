package uk.derbyshire.domain.auth

import uk.derbyshire.domain.users.Role
import kotlin.uuid.Uuid

data class AuthenticatedUser(
    val userId: Uuid,
    val username: String,
    val role: Role,
)