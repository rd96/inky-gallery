package uk.derbyshire.api.me

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.me.QueryCanvasesResponseDTO.Companion.toDto
import uk.derbyshire.api.me.shared.DrawingDTO
import uk.derbyshire.api.me.shared.DrawingDTO.Companion.toDto
import uk.derbyshire.domain.canvases.CanvasWithDrawings
import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.canvases.CanvasStatus
import uk.derbyshire.domain.canvases.CanvasType
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.services.CanvasService
import kotlin.time.Instant

fun queryMyCanvases(canvasService: CanvasService) = { request: Request ->
    val currentUser = CurrentUser(request)

    val queryOptions = QueryCanvasesRequestDTO.lens(request)
    val canvases = canvasService.getMyCanvases(currentUser.userId, queryOptions.canvasStatus)

    Response(Status.OK).with(QueryCanvasesResponseDTO.lens of canvases.toDto())
}

private data class QueryCanvasesRequestDTO(
    val canvasStatus: CanvasStatus?,
) {
    companion object {
        val lens = Json.autoBody<QueryCanvasesRequestDTO>().toLens()
    }
}

private data class QueryCanvasesResponseDTO(
    val canvasId: CanvasId,
    val orientation: Orientation,
    val widthPx: Int,
    val heightPx: Int,
    val status: CanvasStatus,
    val type: CanvasType,
    val createdAt: Instant,
    val drawings: List<DrawingDTO>,
) {
    companion object {
        val lens = Json.autoBody<List<QueryCanvasesResponseDTO>>().toLens()

        fun List<CanvasWithDrawings>.toDto() = map {
            val canvas = it.canvas
            QueryCanvasesResponseDTO(
                canvasId = canvas.canvasId,
                orientation = canvas.orientation,
                widthPx = canvas.widthPx,
                heightPx = canvas.heightPx,
                status = canvas.status,
                type = canvas.type,
                createdAt = canvas.createdAt,
                drawings = it.drawings.toDto(),
            )
        }
    }
}
