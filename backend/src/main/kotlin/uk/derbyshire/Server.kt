package uk.derbyshire

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
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
import uk.derbyshire.api.filters.AuthFilters
import uk.derbyshire.api.filters.requestIdFilter
import uk.derbyshire.api.filters.requestLoggingFilter

class Server(private val services: Services, val serverConfig: ServerConfig) {
    private val logger = LoggerFactory.getLogger(Server::class.java)

    private val catchAllErrorHandler = { e: Throwable ->
        when (e) {
            is IllegalArgumentException -> Response(Status.BAD_REQUEST).body("Invalid input: ${e.message}")
            else -> Response(Status.INTERNAL_SERVER_ERROR).body("Something went wrong, please try again later.")
        }
    }

    fun start() {
        warningsCheck()

        val authFilters = AuthFilters(services.authService)

        val app: HttpHandler = requestIdFilter()
            .then(requestLoggingFilter())
            .then(ServerFilters.RequestTracing())
            .then(routes(
                "/api" bind apiRoutes(authFilters, services.authService, serverConfig),
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
