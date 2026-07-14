package uk.derbyshire

import uk.derbyshire.auth.PasswordHasher
import uk.derbyshire.auth.SessionTokens
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.DatabaseMigrator
import uk.derbyshire.database.DatabaseSetup
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
}

class Services(repositories: Repositories, database: DatabaseContext, clock: Clock) {
    private val passwordHasher = PasswordHasher()
    private val sessionTokens = SessionTokens()

    val userService = UserService(repositories.userRepository, passwordHasher, database)
    val authService = AuthService(repositories.sessionRepository, userService, sessionTokens, passwordHasher, database, clock)
}