package uk.derbyshire.api.me

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.PathParams.canvasId
import uk.derbyshire.services.CanvasService

fun postCompleteCanvas(canvasService: CanvasService) = { request: Request ->
    val currentUser = CurrentUser(request)
    val canvasId = canvasId(request)

    when (val result = canvasService.completeCanvas(currentUser.userId, canvasId)) {
        is Success -> Response(Status.NO_CONTENT)
        is Failure -> result.reason.description.toErrorResponseDTO()
    }
}