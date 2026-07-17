package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update
import uk.derbyshire.database.schema.ActivationTokenTable
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ActivationTokenRepository {
    fun createActivationToken(userId: Uuid, tokenHash: String, createdBy: Uuid, expiresAt: Instant) {
        ActivationTokenTable.insert {
            it[this.userId] = userId
            it[this.tokenHash] = tokenHash
            it[this.createdBy] = createdBy
            it[this.expiresAt] = expiresAt
        }
    }

    fun expireActivationTokensForUser(userId: Uuid, revokedAt: Instant) {
        ActivationTokenTable.update({
            (ActivationTokenTable.userId eq userId) and (ActivationTokenTable.revokedAt.isNull() and (ActivationTokenTable.usedAt.isNull()))
        }) {
            it[this.revokedAt] = revokedAt
        }
    }
}