package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp

object ActivationTokenTable : UuidTable("activation_tokens") {
    val userId = reference("user_id", UserTable.id, ReferenceOption.CASCADE)
    val tokenHash = varchar("token_hash", 64).uniqueIndex()

    val createdAt = timestamp("created_at")
    val createdBy = reference("created_by", UserTable.id)

    val expiresAt = timestamp("expires_at")
    val usedAt = timestamp("used_at").nullable()
    val revokedAt = timestamp("revoked_at").nullable()
}