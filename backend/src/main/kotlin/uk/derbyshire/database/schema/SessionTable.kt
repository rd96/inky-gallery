package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object SessionTable : UuidTable("sessions") {
    val userId = reference("user_id", UserTable, ReferenceOption.CASCADE).index()
    val tokenHash = varchar("token_hash", length = 64).uniqueIndex()

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val expiresAt = timestamp("expires_at")
    val lastSeenAt = timestamp("last_seen_at").defaultExpression(CurrentTimestamp)

    val userAgent = varchar("user_agent", length = 512).nullable()
    val ipAddress = varchar("ip_address", length = 45).nullable()
}