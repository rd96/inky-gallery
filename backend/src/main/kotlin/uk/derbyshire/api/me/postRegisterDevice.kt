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
import uk.derbyshire.domain.devices.DeviceId
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.services.DeviceService

fun postRegisterDevice(deviceService: DeviceService) = { request: Request ->
    val currentUser = CurrentUser(request)

    val registerDeviceRequest = PostRegisterDeviceRequestDTO.lens(request)

    val result = deviceService.registerDevice(
        userId = currentUser.userId,
        deviceModelId = registerDeviceRequest.deviceModelId,
        deviceNickname = registerDeviceRequest.deviceNickname,
        orientation = registerDeviceRequest.orientation,
    )

    when (result) {
        is Success -> Response(Status.OK).with(PostRegisterDeviceResponseDTO.lens of PostRegisterDeviceResponseDTO(result.value))
        is Failure -> result.reason.description.toErrorResponseDTO()
    }
}

data class PostRegisterDeviceRequestDTO(
    val deviceNickname: String,
    val deviceModelId: DeviceModelId,
    val orientation: Orientation,
) {
    companion object {
        val lens = Json.autoBody<PostRegisterDeviceRequestDTO>().toLens()
    }
}

data class PostRegisterDeviceResponseDTO(
    val deviceId: DeviceId,
) {
    companion object {
        val lens = Json.autoBody<PostRegisterDeviceResponseDTO>().toLens()
    }
}



