package uk.derbyshire.api.me

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.me.DrawingResponseDTO.Companion.toDto
import uk.derbyshire.api.me.QueryCanvasesResponseDTO.Companion.toDto
import uk.derbyshire.domain.canvases.CanvasDetail
import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.canvases.CanvasStatus
import uk.derbyshire.domain.canvases.CanvasType
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.drawings.DrawingId
import uk.derbyshire.domain.drawings.DrawingMetadata
import uk.derbyshire.domain.users.UserId
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
    val drawings: List<DrawingResponseDTO>,
    val sentTo: List<UserId>,
) {
    companion object {
        val lens = Json.autoBody<List<QueryCanvasesResponseDTO>>().toLens()

        fun List<CanvasDetail>.toDto() = map {
            val canvas = it.canvasMetadata
            QueryCanvasesResponseDTO(
                canvasId = canvas.canvasId,
                orientation = canvas.orientation,
                widthPx = canvas.widthPx,
                heightPx = canvas.heightPx,
                status = canvas.status,
                type = canvas.type,
                createdAt = canvas.createdAt,
                drawings = it.drawings.toDto(),
                sentTo = emptyList(),
            )
        }
    }
}

private data class DrawingResponseDTO(
    val drawingId: DrawingId,
    val position: Int,
    val createdAt: Instant,
) {
    companion object {
        fun List<DrawingMetadata>.toDto() = map {
            DrawingResponseDTO(
                drawingId = it.drawingId,
                position = it.position,
                createdAt = it.createdAt,
            )
        }
    }
}