package uk.derbyshire.domain.devices

data class UserDevice(
    val deviceId: DeviceId,
    val deviceNickname: String,
    val modelName: String,
    private val landscapeWidthPx: Int,
    private val landscapeHeightPx: Int,
    val orientation: Orientation,
    val colourSwatch: List<HexColour>?,
    val enabled: Boolean,
) {
    val widthPx = when (orientation) {
        Orientation.LANDSCAPE -> landscapeWidthPx
        Orientation.PORTRAIT -> landscapeHeightPx
    }

    val heightPx = when (orientation) {
        Orientation.LANDSCAPE -> landscapeHeightPx
        Orientation.PORTRAIT -> landscapeWidthPx
    }
}