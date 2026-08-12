package uk.derbyshire.domain.devices

data class DeviceModel(
    val deviceModelId: DeviceModelId,
    val modelName: String,
    val landscapeWidthPx: Int,
    val landscapeHeightPx: Int,
    val palette: Palette?,
)