package uk.derbyshire.api.admin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.admin.GetUserConnectionsResponseDTO.Companion.toDto
import uk.derbyshire.api.admin.UserConnectionDTO.Companion.toDto
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.Path
import uk.derbyshire.domain.connections.UserConnection
import uk.derbyshire.domain.connections.UserConnections
import uk.derbyshire.domain.users.UserId
import uk.derbyshire.services.ConnectionService
import kotlin.uuid.Uuid

fun getUserConnections(connectionService: ConnectionService) = request@{ request: Request ->
    val userId = Path.userId(request)

    when (val connections = connectionService.getAllConnectionsForUser(userId)) {
        is Success -> Response(Status.OK).with(GetUserConnectionsResponseDTO.lens of connections.value.toDto())
        is Failure -> connections.reason.description.toErrorResponseDTO(Status.NOT_FOUND)
    }
}

data class GetUserConnectionsResponseDTO(
    val senders: List<UserConnectionDTO>,
    val recipients: List<UserConnectionDTO>,
) {
    companion object {
        val lens = Json.autoBody<GetUserConnectionsResponseDTO>().toLens()

        fun UserConnections.toDto() = GetUserConnectionsResponseDTO(
            senders = senders.map { it.toDto() },
            recipients = recipients.map { it.toDto() },
        )
    }
}

data class UserConnectionDTO(
    val connectionId: Uuid,
    val userId: UserId,
    val username: String,
    val displayName: String,
    val enabled: Boolean,
) {
    companion object {
        fun UserConnection.toDto() = UserConnectionDTO(
            connectionId = connectionId,
            userId = userId,
            username = username,
            displayName = displayName,
            enabled = enabled,
        )
    }
}

