package uk.derbyshire.database

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.slf4j.LoggerFactory
import uk.derbyshire.AdminUserConfig
import uk.derbyshire.domain.users.CreateAdminFailure
import uk.derbyshire.services.UserService

class DatabaseSetup(
    private val database: DatabaseContext,
    private val adminUserConfig: AdminUserConfig?,
    private val userService: UserService,
) {
    private val logger = LoggerFactory.getLogger(DatabaseSetup::class.java)

    fun run() {
        database.transaction {
            if (adminUserConfig != null) {
                val username = adminUserConfig.username.use { it }

                when (val result = userService.createInitialAdminUser(username, adminUserConfig.password)) {
                    is Success -> logger.info("Admin user created successfully")
                    is Failure -> when (result.reason) {
                        CreateAdminFailure.ADMIN_ALREADY_EXISTS -> logger.info("Admin user already exists, skipping")
                        else -> logger.warn("Admin user creation failed, reason: ${result.reason}")
                    }
                }
            } else {
                if (!userService.hasAdminUser()) logger.error("No admin user exists and no set up config found")
            }
        }
    }
}