package uk.derbyshire.api.me

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.me.GetReceivedCanvasMessagesResponseDTO.Companion.toDto
import uk.derbyshire.api.me.shared.DrawingDTO
import uk.derbyshire.api.me.shared.DrawingDTO.Companion.toDto
import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.drawings.Drawing
import uk.derbyshire.domain.messages.CanvasMessage
import uk.derbyshire.domain.messages.CanvasMessageWithDrawings
import uk.derbyshire.domain.messages.MessageId
import uk.derbyshire.domain.users.UserId
import uk.derbyshire.services.MessageService
import kotlin.time.Instant

fun getReceivedCanvasMessages(messageService: MessageService) = { request: Request ->
    val currentUser = CurrentUser(request)

    val messages = messageService.getReceivedCanvasMessages(currentUser.userId)

    Response(Status.OK).with(GetReceivedCanvasMessagesResponseDTO.lens of messages.toDto())
}

private data class GetReceivedCanvasMessagesResponseDTO(
    val messageId: MessageId,
    val fromUserId: UserId,
    val fromDisplayName: String,
    val fromUsername: String,
    val message: String?,
    val showName: Boolean,
    val sentAt: Instant,
    val canvasId: CanvasId,
    val widthPx: Int,
    val heightPx: Int,
    val drawings: List<DrawingDTO>,
) {
    companion object {
        val lens = Json.autoBody<List<GetReceivedCanvasMessagesResponseDTO>>().toLens()

        fun List<CanvasMessageWithDrawings>.toDto() = map { (canvas: CanvasMessage, drawings: List<Drawing>) ->
            GetReceivedCanvasMessagesResponseDTO(
                messageId = canvas.messageId,
                fromUserId = canvas.fromUserId,
                fromDisplayName = canvas.fromDisplayName,
                fromUsername = canvas.fromUsername,
                message = canvas.message,
                showName = canvas.showName,
                sentAt = canvas.sentAt,
                canvasId = canvas.canvasId,
                widthPx = canvas.widthPx,
                heightPx = canvas.heightPx,
                drawings = drawings.toDto(),
            )
        }
    }
}
