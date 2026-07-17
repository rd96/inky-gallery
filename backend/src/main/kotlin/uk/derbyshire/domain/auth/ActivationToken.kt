package uk.derbyshire.domain.auth

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class ActivationToken(
    val userId: Uuid,
    val expiresAt: Instant,
    val usedAt: Instant?,
    val revokedAt: Instant?,
) {
    fun isValid(now: Instant): Boolean = usedAt == null && revokedAt == null && expiresAt > now
}