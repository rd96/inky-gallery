package uk.derbyshire.api.admin

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.api.helpers.Path
import uk.derbyshire.services.ConnectionsService

fun deleteUserConnection(connectionsService: ConnectionsService) = { request: Request ->
    val connectionId = Path.connectionId(request)

    connectionsService.deleteUserConnection(connectionId)

    Response(Status.NO_CONTENT)
}