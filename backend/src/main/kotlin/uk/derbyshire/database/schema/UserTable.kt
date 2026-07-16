package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.UserStatus
import uk.derbyshire.services.UserService

object UserTable : UuidTable("users") {
    val username = varchar("username", length = UserService.MAX_USERNAME_LENGTH).uniqueIndex()
    val passwordHash = varchar("password_hash", length = 255).nullable()

    val displayName = varchar("display_name", length = 100)

    val role = enumerationByName<Role>("role", 20).default(Role.USER)
    val status = enumerationByName<UserStatus>("status", 20).default(UserStatus.PENDING_ACTIVATION)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        check("users_password_hash_matches_status") {
            ((status eq UserStatus.PENDING_ACTIVATION) and passwordHash.isNull()
                or ((status neq UserStatus.PENDING_ACTIVATION) and passwordHash.isNotNull()))
        }
    }
}