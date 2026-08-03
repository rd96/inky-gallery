package uk.derbyshire.api.me

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.ContentType
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.PathParams.canvasId
import uk.derbyshire.api.helpers.RequestMediaChecks.hasContentType
import uk.derbyshire.api.helpers.RequestMediaChecks.readBodyUpTo
import uk.derbyshire.domain.drawings.DrawingId
import uk.derbyshire.services.DrawingService

private const val MAX_DRAWING_BYTES = 512 * 1024

fun postCreateDrawing(drawingService: DrawingService) = request@{ request: Request ->
    val currentUser = CurrentUser(request)
    val canvasId = canvasId(request)

    if (!request.hasContentType(ContentType.IMAGE_PNG)) return@request Response(Status.UNSUPPORTED_MEDIA_TYPE)
    val uploadedBytes = request.readBodyUpTo(MAX_DRAWING_BYTES) ?: return@request Response(Status.REQUEST_ENTITY_TOO_LARGE)

    when (val result = drawingService.saveDrawing(currentUser.userId, canvasId, uploadedBytes)) {
        is Success -> Response(Status.OK).with(PostCreateDrawingResponseDTO.lens of PostCreateDrawingResponseDTO(result.value))
        is Failure -> result.reason.description.toErrorResponseDTO()
    }
}

data class PostCreateDrawingResponseDTO(
    val drawingId: DrawingId,
) {
    companion object {
        val lens = Json.autoBody<PostCreateDrawingResponseDTO>().toLens()

    }
}

