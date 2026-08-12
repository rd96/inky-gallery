package uk.derbyshire.domain.devices

data class UserDevice(
    val deviceId: DeviceId,
    val deviceNickname: String,
    val deviceModelId: DeviceModelId,
    val deviceModelName: String,
    override val landscapeWidthPx: Int,
    override val landscapeHeightPx: Int,
    override val orientation: Orientation,
    val palette: Palette?,
    val enabled: Boolean,
) : Dimensions