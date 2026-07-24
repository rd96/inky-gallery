package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object DeviceApiKeyTable : UuidTable("device_api_keys") {
    val deviceId = reference("deviceId", DeviceTable, ReferenceOption.CASCADE)
    val keyHash = varchar("key_hash", 64).uniqueIndex()
    val keyPrefix = varchar("api_key_reference", 10)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val revokedAt = timestamp("revoked_at").defaultExpression(CurrentTimestamp)
}