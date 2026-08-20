package uk.derbyshire.domain.auth

import uk.derbyshire.domain.users.UserId
import kotlin.time.Instant

data class AccountToken(
    val userId: UserId,
    val expiresAt: Instant,
    val usedAt: Instant?,
    val revokedAt: Instant?,
) {
    fun isValid(now: Instant): Boolean = usedAt == null && revokedAt == null && expiresAt > now
}