package uk.derbyshire.domain.devices

import uk.derbyshire.services.DeviceService
import uk.derbyshire.services.DeviceService.Companion.MAX_DEVICE_MODEL_NAME_LENGTH

enum class CreateDeviceModelFailure(val description: String) {
    INVALID_WIDTH("Width must be between ${DeviceService.MIN_DEVICE_DIMENSION_PX} and ${DeviceService.MAX_DEVICE_DIMENSION_PX}"),
    INVALID_HEIGHT("Height must be between ${DeviceService.MIN_DEVICE_DIMENSION_PX} and ${DeviceService.MAX_DEVICE_DIMENSION_PX}"),
    TOTAL_SIZE_TOO_LARGE("Total pixel size must not exceed ${DeviceService.MAX_TOTAL_PIXELS} pixels"),
    DIMENSIONS_NOT_IN_LANDSCAPE("Landscape width must be greater than or equal to height"),
    MODAL_NAME_TOO_LONG("Modal name is to long, max $MAX_DEVICE_MODEL_NAME_LENGTH"),
    MODEL_NAME_ALREADY_TAKEN("Model name already taken"),
}