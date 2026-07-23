package uk.derbyshire.api.users

import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.services.ConnectionsService

fun userRoutes(connectionsService: ConnectionsService) = routes(
    "/connections" bind Method.GET to getConnections(connectionsService),
)