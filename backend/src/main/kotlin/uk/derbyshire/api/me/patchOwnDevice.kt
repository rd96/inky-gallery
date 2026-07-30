package uk.derbyshire.api.me

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.PathParams.deviceId
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.services.DeviceService

fun patchOwnDevice(deviceService: DeviceService) = { request: Request ->
    val currentUser = CurrentUser(request)
    val deviceId = deviceId(request)

    val patchRequest = PatchOwnDeviceRequestDTO.lens(request)

    val result = deviceService.updateDevice(
        userId = currentUser.userId,
        deviceId = deviceId,
        deviceNickname = patchRequest.deviceNickname,
        orientation = patchRequest.orientation,
        enabled = patchRequest.enabled,
    )

    when (result) {
        is Success -> Response(Status.NO_CONTENT)
        is Failure -> result.reason.description.toErrorResponseDTO()
    }
}

data class PatchOwnDeviceRequestDTO(
    val deviceNickname: String? = null,
    val orientation: Orientation? = null,
    val enabled: Boolean? = null,
) {
    companion object {
        val lens = Json.autoBody<PatchOwnDeviceRequestDTO>().toLens()
    }
}