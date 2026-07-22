package uk.derbyshire.domain.auth

import uk.derbyshire.domain.users.UserId
import kotlin.time.Instant

data class UserPendingActivation(
    val userId: UserId,
    val activationToken: String,
    val expiresAt: Instant,
)