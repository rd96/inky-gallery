package uk.derbyshire

import uk.derbyshire.services.PasswordHasherService
import uk.derbyshire.services.SessionTokenService
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.DatabaseMigrator
import uk.derbyshire.database.DatabaseSetup
import uk.derbyshire.database.repositories.ActivationTokenRepository
import uk.derbyshire.database.repositories.SessionRepository
import uk.derbyshire.database.repositories.UserRepository
import uk.derbyshire.services.AuthService
import uk.derbyshire.services.UserService
import kotlin.time.Clock

class Application {
    private val env = Environment.fromEnv()
    private val clock = Clock.System
    private val database = DatabaseContext(env.databaseConfig)

    private val repositories = Repositories()
    private val services = Services(repositories, database, clock)

    fun start() {
        setupDatabase()
        startServer()
    }

    private fun startServer() {
        val server = Server(services, env.serverConfig)

        server.start()
    }

    private fun setupDatabase() {
        DatabaseMigrator(database.dataSource).migrate()
        DatabaseSetup(database, env.adminUserConfig, services.userService).run()
    }
}

class Repositories {
    val userRepository = UserRepository()
    val sessionRepository = SessionRepository()
    val activationTokenRepository = ActivationTokenRepository()
}

class Services(repositories: Repositories, database: DatabaseContext, clock: Clock) {
    private val passwordHasherService = PasswordHasherService()
    private val sessionTokenService = SessionTokenService()

    val userService = UserService(repositories.userRepository, passwordHasherService, database)
    val authService = AuthService(repositories.sessionRepository, repositories.activationTokenRepository, userService, sessionTokenService, passwordHasherService, database, clock)
}