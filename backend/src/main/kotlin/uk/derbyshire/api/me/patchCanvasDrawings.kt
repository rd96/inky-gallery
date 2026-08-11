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
import uk.derbyshire.domain.drawings.DrawingId
import uk.derbyshire.services.CanvasService

fun patchCanvasDrawings(canvasService: CanvasService) = { request: Request ->
    val currentUser = CurrentUser(request)
    val canvasId = canvasId(request)

    val patchRequest = PatchCanvasDrawingsRequestDTO.lens(request)

    val result = canvasService.reorderCanvasDrawings(currentUser.userId, canvasId, patchRequest.orderedDrawingIds)

    when (result) {
        is Success -> Response(Status.OK)
        is Failure -> result.reason.description.toErrorResponseDTO()
    }
}

data class PatchCanvasDrawingsRequestDTO(
    val orderedDrawingIds: List<DrawingId>,
) {
    companion object {
        val lens = Json.autoBody<PatchCanvasDrawingsRequestDTO>().toLens()
    }
}