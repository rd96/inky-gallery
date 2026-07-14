package uk.derbyshire

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters.PrintRequestAndResponse
import org.http4k.filter.ServerFilters
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.ApacheServer
import org.http4k.server.asServer
import uk.derbyshire.api.apiRoutes
import uk.derbyshire.api.filters.AuthFilters

class Server(private val services: Services, val serverConfig: ServerConfig) {
    private val catchAllErrorHandler = { e: Throwable ->
        when (e) {
            is IllegalArgumentException -> Response(Status.BAD_REQUEST).body("Invalid input: ${e.message}")
            else -> Response(Status.INTERNAL_SERVER_ERROR).body("Something went wrong, please try again later.")
        }
    }

    fun start() {
        warningsCheck()

        val authFilters = AuthFilters(services.authService)

        val app: HttpHandler = ServerFilters.RequestTracing()
            .then(PrintRequestAndResponse(System.out)) // TODO prefer to have a proper logging solution in place
            .then(routes(
                "/api" bind apiRoutes(authFilters, services.authService, serverConfig),
                "/" bind Method.GET to static(ResourceLoader.Classpath("public")),
            ))

        app.asServer(ApacheServer(serverConfig.port)).also {
            println("Running on ${serverConfig.port}")
        }.start()
    }

    private fun warningsCheck() {
        if (!serverConfig.secureSessionCookies) println("WARNING: Secure session cookies is FALSE. Set to TRUE in production.")
    }
}