package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.jdbc.insert
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
}