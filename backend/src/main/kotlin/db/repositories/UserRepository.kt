package uk.derbyshire.db.repositories

import org.jetbrains.exposed.v1.jdbc.insertIgnore
import uk.derbyshire.db.schema.UsersTable

class UserRepository {
    fun createAdminUserIfMissing(username: String, passwordHash: String) {
        UsersTable.insertIgnore {
            it[this.username] = username
            it[this.passwordHash] = passwordHash
            it[isAdmin] = true
        }
    }
}