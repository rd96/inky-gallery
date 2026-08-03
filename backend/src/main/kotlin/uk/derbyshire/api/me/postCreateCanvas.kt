package uk.derbyshire.api.me

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.canvases.CanvasType
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.services.CanvasService

fun postCreateCanvas(canvasService: CanvasService) = { request: Request ->
    val currentUser = CurrentUser(request)

    val canvasRequest = PostCreateCanvasRequestDTO.lens(request)

    val result = canvasService.createCanvas(
        targetDeviceModelId = canvasRequest.targetDeviceModelId,
        orientation = canvasRequest.orientation,
        type = canvasRequest.canvasType,
        createdBy = currentUser.userId,
    )

    when (result) {
        is Success -> Response(Status.OK).with(PostCreateCanvasResponseDTO.lens of PostCreateCanvasResponseDTO(result.value))
        is Failure -> result.reason.description.toErrorResponseDTO()
    }
}

data class PostCreateCanvasRequestDTO(
    val targetDeviceModelId: DeviceModelId,
    val orientation: Orientation,
    val canvasType: CanvasType,
) {
    companion object {
        val lens = Json.autoBody<PostCreateCanvasRequestDTO>().toLens()
    }
}

data class PostCreateCanvasResponseDTO(
    val canvasId: CanvasId,
) {
    companion object {
        val lens = Json.autoBody<PostCreateCanvasResponseDTO>().toLens()
    }
}