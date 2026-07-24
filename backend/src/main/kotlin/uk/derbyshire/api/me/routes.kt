package uk.derbyshire.api.me

import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.services.ConnectionService

fun userRoutes(connectionService: ConnectionService) = routes(
    "/connections" bind Method.GET to getConnections(connectionService),
)