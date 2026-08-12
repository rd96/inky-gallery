package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.onFailure
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.DeviceModelRepository
import uk.derbyshire.database.repositories.DeviceRepository
import uk.derbyshire.domain.devices.CreateDeviceModelFailure
import uk.derbyshire.domain.devices.DeviceId
import uk.derbyshire.domain.devices.DeviceModel
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.devices.OrientedDeviceModel
import uk.derbyshire.domain.devices.Palette
import uk.derbyshire.domain.devices.RegisterDeviceFailure
import uk.derbyshire.domain.devices.UpdateDeviceFailure
import uk.derbyshire.domain.devices.UserDevice
import uk.derbyshire.domain.drawings.DrawingLimits.MAX_DIMENSION_PX
import uk.derbyshire.domain.drawings.DrawingLimits.MAX_TOTAL_PIXELS
import uk.derbyshire.domain.drawings.DrawingLimits.MIN_DIMENSION_PX
import uk.derbyshire.domain.users.UserId

class DeviceService(
    private val deviceModelRepository: DeviceModelRepository,
    private val deviceRepository: DeviceRepository,
    private val context: DatabaseContext,
) {
    fun createDeviceModel(deviceName: String, landscapeWidthPx: Int, landscapeHeightPx: Int, palette: Palette? = null): Result4k<DeviceModelId, CreateDeviceModelFailure> {
        if (deviceName.length > MAX_DEVICE_MODEL_NAME_LENGTH) return Failure(CreateDeviceModelFailure.MODAL_NAME_TOO_LONG)
        validateDimensions(width = landscapeWidthPx, height = landscapeHeightPx).onFailure { return it }

        return context.transaction {
            deviceModelRepository.insertModel(deviceName, landscapeWidthPx, landscapeHeightPx, palette)
        }.asResultOr { CreateDeviceModelFailure.MODEL_NAME_ALREADY_TAKEN }
    }

    fun getDeviceModels(): List<DeviceModel> =
        context.transaction {
            deviceModelRepository.getDeviceModels()
        }

    fun registerDevice(userId: UserId, deviceModelId: DeviceModelId, deviceNickname: String, orientation: Orientation): Result4k<DeviceId, RegisterDeviceFailure> {
        if (deviceNickname.length > MAX_DEVICE_NICKNAME_LENGTH) return Failure(RegisterDeviceFailure.NICKNAME_TOO_LONG)

        return context.transaction {
            deviceRepository.insertDevice(userId, deviceModelId, deviceNickname, orientation)
        }.asResultOr { RegisterDeviceFailure.NICKNAME_ALREADY_IN_USE_FOR_USER }
    }

    fun updateDevice(userId: UserId, deviceId: DeviceId, deviceNickname: String?, orientation: Orientation?, enabled: Boolean?): Result4k<Unit, UpdateDeviceFailure> {
        if (deviceNickname != null && deviceNickname.length > MAX_DEVICE_NICKNAME_LENGTH) return Failure(UpdateDeviceFailure.DEVICE_NICKNAME_TOO_LONG)

        return context.transaction {
            deviceRepository.updateDevice(userId, deviceId, deviceNickname, orientation, enabled)
        }
    }

    fun getDevicesForUser(userId: UserId): List<UserDevice> =
        context.transaction {
            deviceRepository.getDevicesForUser(userId)
        }

    fun getRecipientDeviceModels(userId: UserId): List<OrientedDeviceModel> = context.transaction {
        context.transaction {
            deviceRepository.getActiveRecipientDevicesForUser(userId)
        }
    }

    companion object {
        const val MAX_DEVICE_NICKNAME_LENGTH = 50
        const val MAX_DEVICE_MODEL_NAME_LENGTH = 50

        private fun validateDimensions(width: Int, height: Int): Result4k<Unit, CreateDeviceModelFailure> {
            if (width !in MIN_DIMENSION_PX..MAX_DIMENSION_PX) return CreateDeviceModelFailure.INVALID_WIDTH.asFailure()
            if (height !in MIN_DIMENSION_PX..MAX_DIMENSION_PX) return CreateDeviceModelFailure.INVALID_HEIGHT.asFailure()
            if (width < height) return CreateDeviceModelFailure.DIMENSIONS_NOT_IN_LANDSCAPE.asFailure()
            if (width.toLong() * height > MAX_TOTAL_PIXELS) return CreateDeviceModelFailure.TOTAL_SIZE_TOO_LARGE.asFailure()
            return Success(Unit)
        }
    }
}