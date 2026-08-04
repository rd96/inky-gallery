package uk.derbyshire.domain.devices

data class UserDevice(
    val deviceId: DeviceId,
    val deviceNickname: String,
    val modelName: String,
    override val landscapeWidthPx: Int,
    override val landscapeHeightPx: Int,
    override val orientation: Orientation,
    val palette: List<HexColour>?,
    val enabled: Boolean,
) : Dimensions