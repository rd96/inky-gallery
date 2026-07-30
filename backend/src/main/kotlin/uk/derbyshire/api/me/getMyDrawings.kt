package uk.derbyshire.api.me

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.me.GetMyDrawingsResponseDTO.Companion.toDto
import uk.derbyshire.domain.drawings.DrawingId
import uk.derbyshire.domain.drawings.DrawingMetadata
import uk.derbyshire.services.DrawingService
import kotlin.time.Instant

fun getMyDrawings(drawingService: DrawingService) = { request: Request ->
    val currentUser = CurrentUser(request)

    val drawings = drawingService.getDrawingsForUser(currentUser.userId)

    Response(Status.OK).with(GetMyDrawingsResponseDTO.lens of drawings.toDto())
}

data class GetMyDrawingsResponseDTO(
    val drawingId: DrawingId,
    val widthPx: Int,
    val heightPx: Int,
    val createdAt: Instant,
) {
    companion object {
        val lens = Json.autoBody<List<GetMyDrawingsResponseDTO>>().toLens()

        fun List<DrawingMetadata>.toDto() = map {
            GetMyDrawingsResponseDTO(
                drawingId = it.drawingId,
                widthPx = it.widthPx,
                heightPx = it.heightPx,
                createdAt = it.createdAt,
            )
        }
    }
}