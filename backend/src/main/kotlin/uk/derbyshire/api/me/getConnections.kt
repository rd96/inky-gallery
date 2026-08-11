package uk.derbyshire.api.me

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.me.GetConnectionsResponseDTO.Companion.toDto
import uk.derbyshire.api.me.UserConnectionDTO.Companion.toDto
import uk.derbyshire.domain.connections.UserConnection
import uk.derbyshire.domain.connections.UserConnections
import uk.derbyshire.domain.users.UserId
import uk.derbyshire.services.ConnectionService

fun getConnections(connectionService: ConnectionService) = { request: Request ->
    val currentUser = CurrentUser(request)

    when (val connections = connectionService.getActiveConnectionsForUser(currentUser.userId)) {
        is Success -> Response(Status.OK).with(GetConnectionsResponseDTO.lens of connections.value.toDto())
        is Failure -> connections.reason.description.toErrorResponseDTO(Status.NOT_FOUND)
    }
}

data class GetConnectionsResponseDTO(
    val senders: List<UserConnectionDTO>,
    val recipients: List<UserConnectionDTO>
) {
    companion object {
        val lens = Json.autoBody<GetConnectionsResponseDTO>().toLens()

        fun UserConnections.toDto() = GetConnectionsResponseDTO(
            senders = senders.toDto(),
            recipients = recipients.toDto(),
        )
    }
}

data class UserConnectionDTO(
    val userId: UserId,
    val username: String,
    val displayName: String,
) {
    companion object {
        fun List<UserConnection>.toDto() = map {
            UserConnectionDTO(
                userId = it.userId,
                username = it.username,
                displayName = it.displayName,
            )
        }

        val lens = Json.autoBody<List<UserConnectionDTO>>().toLens()
    }
}