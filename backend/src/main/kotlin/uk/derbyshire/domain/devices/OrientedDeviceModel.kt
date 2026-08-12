package uk.derbyshire.domain.devices

data class OrientedDeviceModel(
    val deviceModelId: DeviceModelId,
    val deviceModelName: String,
    val landscapeWidthPx: Int,
    val landscapeHeightPx: Int,
    val orientations: Set<Orientation>,
    val palette: Palette?,
)
