package uk.derbyshire.api.me

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.PathParams.canvasId
import uk.derbyshire.domain.users.UserId
import uk.derbyshire.services.MessageService

fun postSendCanvas(messageService: MessageService) = { request: Request ->
    val currentUser = CurrentUser(request)
    val canvasId = canvasId(request)

    val postRequest = PostSendCanvasRequestDTO.lens(request)

    val result = messageService.sendMessage(fromUserId = currentUser.userId, toUserId = postRequest.recipientUserId, canvasId, postRequest.message, postRequest.showName)

    when (result) {
        is Success -> Response(Status.OK)
        is Failure -> result.reason.description.toErrorResponseDTO()
    }
}

data class PostSendCanvasRequestDTO(
    val recipientUserId: UserId,
    val message: String?,
    val showName: Boolean,
) {
    companion object {
        val lens = Json.autoBody<PostSendCanvasRequestDTO>().toLens()
    }
}