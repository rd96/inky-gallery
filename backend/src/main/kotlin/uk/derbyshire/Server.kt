package uk.derbyshire

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.ApacheServer
import org.http4k.server.asServer
import org.slf4j.LoggerFactory
import uk.derbyshire.api.apiRoutes
import uk.derbyshire.api.filters.AuthChecker
import uk.derbyshire.api.filters.ErrorHandler
import uk.derbyshire.api.filters.RequestLogger

class Server(private val services: Services, val serverConfig: ServerConfig) {
    private val logger = LoggerFactory.getLogger(Server::class.java)

    fun start() {
        warningsCheck()

        val authChecker = AuthChecker(services.authService)
        val requestLogger = RequestLogger()
        val errorHandler = ErrorHandler()

        val app: HttpHandler = requestLogger.requestIdFilter()
            .then(requestLogger.requestLoggingFilter())
            .then(errorHandler.catchUnexpectedErrors())
            .then(ServerFilters.CatchLensFailure)
            .then(routes(
                "/api" bind apiRoutes(authChecker, services.userService, services.authService, services.connectionService, services.deviceService, services.drawingService, serverConfig),
                "/" bind Method.GET to static(ResourceLoader.Classpath("public")),
            ))

        app.asServer(ApacheServer(serverConfig.port)).also {
            logger.info("Running on port {}", serverConfig.port)
        }.start()
    }

    private fun warningsCheck() {
        if (!serverConfig.secureSessionCookies) logger.warn("Secure session cookies is FALSE. Set to TRUE in production.")
    }
}
