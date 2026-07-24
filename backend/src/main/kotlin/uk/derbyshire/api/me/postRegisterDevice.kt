package uk.derbyshire.api.me

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.services.DeviceService
import kotlin.uuid.Uuid

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
        is Success -> Response(Status.NO_CONTENT)
        is Failure -> result.reason.description.toErrorResponseDTO()
    }
}

data class PostRegisterDeviceRequestDTO(
    val deviceNickname: String,
    val deviceModelId: Uuid,
    val orientation: Orientation,
) {
    companion object {
        val lens = Json.autoBody<PostRegisterDeviceRequestDTO>().toLens()
    }
}



