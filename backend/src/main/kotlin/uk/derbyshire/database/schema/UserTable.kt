package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import uk.derbyshire.auth.Role
import uk.derbyshire.services.UserService

object UserTable : UuidTable("users") {
    val username = varchar("username", length = UserService.MAX_USERNAME_LENGTH).uniqueIndex()
    val passwordHash = varchar("password_hash", length = 255)

    val role = enumerationByName<Role>("role", 20).default(Role.USER)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val disabled = bool("disabled").default(false)
}