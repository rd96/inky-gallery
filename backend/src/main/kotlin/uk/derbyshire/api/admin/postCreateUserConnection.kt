package uk.derbyshire.api.admin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.domain.users.UserId
import uk.derbyshire.services.ConnectionsService

fun postCreateUserConnection(connectionsService: ConnectionsService) = { request: Request ->
    val user = CurrentUser(request)
    val connectionRequest = PostCreateUserConnectionRequestDTO.lens(request)

    val result = connectionsService.createUserConnection(
        senderUserId = connectionRequest.senderUserId,
        recipientUserId = connectionRequest.recipientUserId,
        createdBy = user.userId,
    )

    when (result) {
        is Success -> Response(Status.NO_CONTENT)
        is Failure -> result.reason.description.toErrorResponseDTO()
    }
}

private data class PostCreateUserConnectionRequestDTO(
    val senderUserId: UserId,
    val recipientUserId: UserId,
) {
    companion object {
        val lens = Json.autoBody<PostCreateUserConnectionRequestDTO>().toLens()
    }
}

