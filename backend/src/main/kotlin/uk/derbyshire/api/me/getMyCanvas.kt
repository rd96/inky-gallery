package uk.derbyshire.api.me

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.PathParams.canvasId
import uk.derbyshire.api.me.GetMyCanvasResponseDTO.Companion.toDto
import uk.derbyshire.domain.canvases.CanvasMetadata
import uk.derbyshire.domain.canvases.CanvasStatus
import uk.derbyshire.domain.canvases.CanvasType
import uk.derbyshire.domain.devices.HexColour
import uk.derbyshire.domain.devices.Palette
import uk.derbyshire.services.CanvasService

fun getMyCanvas(canvasService: CanvasService) = handler@{ request: Request ->
    val currentUser = CurrentUser(request)
    val canvasId = canvasId(request)

    val canvas = canvasService.getMyCanvas(currentUser.userId, canvasId)
        ?: return@handler Response(Status.NOT_FOUND)

    Response(Status.OK).with(GetMyCanvasResponseDTO.lens of canvas.toDto())
}

data class GetMyCanvasResponseDTO(
    val widthPx: Int,
    val heightPx: Int,
    val status: CanvasStatus,
    val type: CanvasType,
    val palette: Palette?,
) {
    companion object {
        val lens = Json.autoBody<GetMyCanvasResponseDTO>().toLens()

        fun CanvasMetadata.toDto() = GetMyCanvasResponseDTO(
            widthPx = this.widthPx,
            heightPx = this.heightPx,
            status = this.status,
            type = this.type,
            palette = this.palette,
        )
    }
}