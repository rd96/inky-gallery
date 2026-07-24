package uk.derbyshire.services

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.onFailure
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.DeviceModelRepository
import uk.derbyshire.database.repositories.DeviceRepository
import uk.derbyshire.domain.devices.CreateDeviceModelFailure
import uk.derbyshire.domain.devices.DeviceModel
import uk.derbyshire.domain.devices.HexColour
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.users.UserId
import kotlin.uuid.Uuid

class DeviceService(
    private val deviceModelRepository: DeviceModelRepository,
    private val deviceRepository: DeviceRepository,
    private val context: DatabaseContext,
) {
    fun createDeviceModel(deviceName: String, landscapeWidthPx: Int, landscapeHeightPx: Int, colourSwatch: List<HexColour>? = null): Result4k<Uuid, CreateDeviceModelFailure> {
        validateDimensions(width = landscapeWidthPx, height = landscapeHeightPx).onFailure { return it }

        return context.transaction {
            deviceModelRepository.insertModel(deviceName, landscapeWidthPx, landscapeHeightPx, colourSwatch)
        }?.let(::Success) ?: CreateDeviceModelFailure.MODEL_NAME_ALREADY_TAKEN.asFailure()
    }


    fun getDeviceModels(): List<DeviceModel> =
        context.transaction {
            deviceModelRepository.getDeviceModels()
        }

    fun registerDevice(deviceModelId: Uuid, userId: UserId, deviceName: String, orientation: Orientation) =
        context.transaction {
            deviceRepository.insertDevice(deviceModelId, userId, deviceName, orientation)
        }

    companion object {
        const val MAX_DEVICE_DIMENSION_PX = 10_000
        const val MIN_DEVICE_DIMENSION_PX = 100

        const val MAX_TOTAL_PIXELS = 10_000_000L

        private fun validateDimensions(width: Int, height: Int): Result4k<Unit, CreateDeviceModelFailure> {
            if (width !in MIN_DEVICE_DIMENSION_PX..MAX_DEVICE_DIMENSION_PX) return CreateDeviceModelFailure.INVALID_WIDTH.asFailure()
            if (height !in MIN_DEVICE_DIMENSION_PX..MAX_DEVICE_DIMENSION_PX) return CreateDeviceModelFailure.INVALID_HEIGHT.asFailure()
            if (width < height) return CreateDeviceModelFailure.DIMENSIONS_NOT_IN_LANDSCAPE.asFailure()
            if (width.toLong() * height > MAX_TOTAL_PIXELS) return CreateDeviceModelFailure.TOTAL_SIZE_TOO_LARGE.asFailure()
            return Success(Unit)
        }
    }
}