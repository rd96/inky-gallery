package uk.derbyshire.domain.auth

import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.UserId

data class AuthenticatedUser(
    val userId: UserId,
    val username: String,
    val role: Role,
    val displayName: String
)
