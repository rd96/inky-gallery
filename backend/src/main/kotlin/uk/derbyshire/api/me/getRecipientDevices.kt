package uk.derbyshire.api.me

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.PathParams.userId
import uk.derbyshire.api.me.GetRecipientDevicesResponseDTO.Companion.toDto
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.devices.Palette
import uk.derbyshire.domain.devices.UserDevice
import uk.derbyshire.services.DeviceService

fun getRecipientDevices(deviceService: DeviceService) = { request: Request ->
    val currentUser = CurrentUser(request)
    val recipientUserId = userId(request)

    val availableDeviceModels = deviceService.getRecipientDeviceModels(currentUser.userId, recipientUserId)

    Response(Status.OK).with(GetRecipientDevicesResponseDTO.lens of availableDeviceModels.toDto())
}

data class GetRecipientDevicesResponseDTO(
    val deviceModelId: DeviceModelId,
    val deviceModelName: String,
    val landscapeWidthPx: Int,
    val landscapeHeightPx: Int,
    val orientation: Orientation,
    val palette: Palette?,
) {
    companion object {
        val lens = Json.autoBody<List<GetRecipientDevicesResponseDTO>>().toLens()

        fun List<UserDevice>.toDto() = map {
            GetRecipientDevicesResponseDTO(
                deviceModelId = it.deviceModelId,
                deviceModelName = it.deviceModelName,
                landscapeWidthPx = it.landscapeWidthPx,
                landscapeHeightPx = it.landscapeHeightPx,
                orientation = it.orientation,
                palette = it.palette,
            )
        }
    }}