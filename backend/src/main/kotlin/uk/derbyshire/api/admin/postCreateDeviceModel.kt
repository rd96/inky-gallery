package uk.derbyshire.api.admin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.Palette
import uk.derbyshire.services.DeviceService

fun postCreateDeviceModel(deviceService: DeviceService) = { request: Request ->
    val deviceModel = PostCreateDeviceModelRequestDTO.lens(request)

    val deviceModelResult = deviceService.createDeviceModel(
        deviceName = deviceModel.deviceName,
        landscapeWidthPx = deviceModel.landscapeWidthPx,
        landscapeHeightPx = deviceModel.landscapeHeightPx,
        palette = deviceModel.palette,
    )

    when (deviceModelResult) {
        is Success -> Response(Status.OK).with(PostCreateDeviceModelResponseDTO.lens of PostCreateDeviceModelResponseDTO(deviceModelResult.value))
        is Failure -> deviceModelResult.reason.description.toErrorResponseDTO()
    }
}

data class PostCreateDeviceModelRequestDTO(
    val deviceName: String,
    val landscapeWidthPx: Int,
    val landscapeHeightPx: Int,
    val palette: Palette? = null,
) {
    companion object {
        val lens = Json.autoBody<PostCreateDeviceModelRequestDTO>().toLens()
    }
}

data class PostCreateDeviceModelResponseDTO(
    val deviceModelId: DeviceModelId,
) {
    companion object {
        val lens = Json.autoBody<PostCreateDeviceModelResponseDTO>().toLens()
    }
}
