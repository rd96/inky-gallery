package uk.derbyshire.database

import uk.derbyshire.AdminUserConfig
import uk.derbyshire.auth.PasswordHasher
import uk.derbyshire.services.UserService

class DatabaseSetup(
    private val database: DatabaseContext,
    private val adminUserConfig: AdminUserConfig?,
    private val userService: UserService,
) {
    fun run() {
        database.transaction {
            if (adminUserConfig != null) {
                val username = adminUserConfig.username.use { it }

                userService.createAdminUserIfMissing(username, adminUserConfig.password)
            }
        }
    }
}