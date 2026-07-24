package uk.derbyshire.api.me

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.me.GetMyDevicesResponseDTO.Companion.toDto
import uk.derbyshire.domain.devices.HexColour
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.devices.UserDevice
import uk.derbyshire.services.DeviceService
import kotlin.uuid.Uuid

fun getMyDevices(deviceService: DeviceService) = { request: Request ->
    val currentUser = CurrentUser(request)

    val devices = deviceService.getDevicesForUser(currentUser.userId)

    Response(Status.OK).with(GetMyDevicesResponseDTO.lens of devices.toDto())
}

data class GetMyDevicesResponseDTO(
    val deviceId: Uuid,
    val modelName: String,
    val deviceNickname: String,
    val widthPx: Int,
    val heightPx: Int,
    val orientation: Orientation,
    val colourSwatch: List<HexColour>? = null,
    val enabled: Boolean,
) {
    companion object {
        val lens = Json.autoBody<List<GetMyDevicesResponseDTO>>().toLens()

        fun List<UserDevice>.toDto() = map {
            GetMyDevicesResponseDTO(
                deviceId = it.deviceId,
                modelName = it.modelName,
                deviceNickname = it.deviceNickname,
                widthPx = it.widthPx,
                heightPx = it.heightPx,
                orientation = it.orientation,
                colourSwatch = it.colourSwatch,
                enabled = it.enabled,
            )
        }
    }
}