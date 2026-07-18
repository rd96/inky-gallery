package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import uk.derbyshire.domain.users.ActivationStatus
import uk.derbyshire.domain.users.Role
import uk.derbyshire.services.UserService

object UserTable : UuidTable("users") {
    val username = varchar("username", length = UserService.MAX_USERNAME_LENGTH).uniqueIndex()
    val passwordHash = varchar("password_hash", length = 255).nullable()

    val displayName = varchar("display_name", length = UserService.MAX_DISPLAY_NAME_LENGTH)

    val role = enumerationByName<Role>("role", 20).default(Role.USER)
    val activationStatus = enumerationByName<ActivationStatus>("activation_status", 20).default(ActivationStatus.PENDING)
    val enabled = bool("enabled").default(true)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        check("users_password_hash_matches_activation_status") {
            ((activationStatus eq ActivationStatus.PENDING) and passwordHash.isNull()
                or ((activationStatus eq ActivationStatus.ACTIVATED) and passwordHash.isNotNull()))
        }
    }
}