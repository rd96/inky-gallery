package uk.derbyshire.domain.devices

import uk.derbyshire.services.DeviceService.Companion.MAX_DEVICE_NICKNAME_LENGTH

enum class UpdateDeviceFailure(val description: String) {
    DEVICE_NOT_FOUND("Device not found"),
    DEVICE_NICKNAME_TOO_LONG("Device nickname too long, max $MAX_DEVICE_NICKNAME_LENGTH"),
    DEVICE_NICKNAME_IN_USE_FOR_USER("Device nickname is already in use"),
}