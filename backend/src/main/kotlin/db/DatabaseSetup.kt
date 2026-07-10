package uk.derbyshire.db

import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import uk.derbyshire.AdminUserConfig
import uk.derbyshire.db.repositories.UserRepository
import uk.derbyshire.db.schema.UsersTable
import uk.derbyshire.db.security.PasswordHasher

class DatabaseSetup(
    private val database: Database,
    private val adminUserConfig: AdminUserConfig?,
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
) {
    fun run() {
        database.transaction {
            SchemaUtils.createMissingTablesAndColumns(UsersTable)

            if (adminUserConfig != null) {
                val username = adminUserConfig.username.use { it }
                val passwordHash = adminUserConfig.password.use { passwordHasher.hash(it) }

                userRepository.createAdminUserIfMissing(username, passwordHash)
            }
        }
    }
}