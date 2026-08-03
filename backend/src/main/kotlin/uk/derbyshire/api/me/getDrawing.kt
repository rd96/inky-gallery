package uk.derbyshire.api.me

import org.http4k.core.ContentType
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.contentType
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.PathParams.canvasId
import uk.derbyshire.api.helpers.PathParams.drawingId
import uk.derbyshire.services.DrawingService

fun getDrawing(drawingService: DrawingService) = handler@{ request: Request ->
    val currentUser = CurrentUser(request)
    val canvasId = canvasId(request)
    val drawingId = drawingId(request)

    val drawing = drawingService.getDrawingForUser(currentUser.userId, canvasId, drawingId)
        ?: return@handler Response(Status.NOT_FOUND)

    Response(Status.OK)
        .contentType(ContentType.IMAGE_PNG)
        .header("Content-Length", drawing.size.toString())
        .body(drawing.inputStream())
}