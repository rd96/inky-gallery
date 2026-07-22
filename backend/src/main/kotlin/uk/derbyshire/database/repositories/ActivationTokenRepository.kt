package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import uk.derbyshire.database.schema.ActivationTokenTable
import uk.derbyshire.domain.auth.ActivationToken
import uk.derbyshire.domain.users.UserId
import kotlin.time.Instant

class ActivationTokenRepository {
    fun createActivationToken(userId: UserId, tokenHash: String, expiresAt: Instant, createdBy: UserId, createdAt: Instant) {
        ActivationTokenTable.insert {
            it[this.userId] = userId.value
            it[this.tokenHash] = tokenHash
            it[this.createdAt] = createdAt
            it[this.createdBy] = createdBy.value
            it[this.expiresAt] = expiresAt
        }
    }

    fun revokeActivationTokensForUser(userId: UserId, revokedAt: Instant) {
        ActivationTokenTable.update({
            (ActivationTokenTable.userId eq userId.value) and (ActivationTokenTable.revokedAt.isNull() and (ActivationTokenTable.usedAt.isNull()))
        }) {
            it[this.revokedAt] = revokedAt
        }
    }

    fun getByTokenHash(tokenHash: String): ActivationToken? =
        ActivationTokenTable.select(
            ActivationTokenTable.userId,
            ActivationTokenTable.expiresAt,
            ActivationTokenTable.usedAt,
            ActivationTokenTable.revokedAt,
        ).where { ActivationTokenTable.tokenHash eq tokenHash }
            .singleOrNull()
            ?.let {
                ActivationToken(
                    UserId(it[ActivationTokenTable.userId].value),
                    it[ActivationTokenTable.expiresAt],
                    it[ActivationTokenTable.usedAt],
                    it[ActivationTokenTable.revokedAt],
                )
            }

}