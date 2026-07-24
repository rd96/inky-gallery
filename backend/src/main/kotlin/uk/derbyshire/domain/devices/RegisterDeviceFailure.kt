package uk.derbyshire.domain.devices

import uk.derbyshire.services.DeviceService.Companion.MAX_DEVICE_NICKNAME_LENGTH

enum class RegisterDeviceFailure(val description: String) {
    NICKNAME_TOO_LONG("Device nickname is too long, max $MAX_DEVICE_NICKNAME_LENGTH"),
    NICKNAME_ALREADY_IN_USE_FOR_USER("Device nickname already in use"),
}