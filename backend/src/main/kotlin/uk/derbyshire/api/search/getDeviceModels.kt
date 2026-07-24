package uk.derbyshire.api.search

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.search.GetDeviceModelsResponseDTO.Companion.toDto
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.domain.devices.DeviceModel
import uk.derbyshire.domain.devices.HexColour
import uk.derbyshire.services.DeviceService
import kotlin.uuid.Uuid

fun getDeviceModels(deviceService: DeviceService): HttpHandler = {
    val devices = deviceService.getDeviceModels()

    Response(Status.OK).with(GetDeviceModelsResponseDTO.lens of devices.toDto())
}

data class GetDeviceModelsResponseDTO(
    val deviceModelId: Uuid,
    val deviceName: String,
    val landscapeWidthPx: Int,
    val landscapeHeightPx: Int,
    val colourSwatch: List<HexColour>? = null,
) {
    companion object {
        val lens = Json.autoBody<List<GetDeviceModelsResponseDTO>>().toLens()

        fun List<DeviceModel>.toDto() = map {
            GetDeviceModelsResponseDTO(
                deviceModelId = it.deviceModelId,
                deviceName = it.modelName,
                landscapeWidthPx = it.landscapeWidthPx,
                landscapeHeightPx = it.landscapeHeightPx,
                colourSwatch = it.colourSwatch,
            )
        }
    }
}
