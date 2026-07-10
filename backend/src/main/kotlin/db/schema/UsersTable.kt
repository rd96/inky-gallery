package uk.derbyshire.db.schema

import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object UsersTable : UuidTable("users") {
    val username = varchar("username", length = 320).uniqueIndex()
    val passwordHash = varchar("password_hash", length = 100)
    val isAdmin = bool("is_admin").default(false)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}