package uk.derbyshire.database.repositories

import org.http4k.config.Secret
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import uk.derbyshire.auth.Role
import uk.derbyshire.database.schema.UserTable
import uk.derbyshire.services.User

class UserRepository {
    fun createUser(username: String, passwordHash: String) =
        UserTable.insertAndGetId {
            it[this.username] = username
            it[this.passwordHash] = passwordHash
        }.value

    fun hasAdminUser(): Boolean =
        UserTable
            .select(UserTable.id)
            .where { UserTable.role eq Role.ADMIN }
            .limit(1)
            .any()

    fun createAdminUser(username: String, passwordHash: String) {
        UserTable.insert {
            it[this.username] = username
            it[this.passwordHash] = passwordHash
            it[this.role] = Role.ADMIN
        }
    }

    fun findUserByUsername(username: String): User? =
        UserTable.select(
            UserTable.id,
            UserTable.username,
            UserTable.passwordHash,
            UserTable.role,
            UserTable.createdAt,
            UserTable.disabled
        )
            .where { UserTable.username eq username }
            .singleOrNull()
            ?.let {
                User(
                    it[UserTable.id].value,
                    it[UserTable.username],
                    Secret(it[UserTable.passwordHash]),
                    it[UserTable.role],
                    it[UserTable.createdAt],
                    it[UserTable.disabled],
                )
            }
}