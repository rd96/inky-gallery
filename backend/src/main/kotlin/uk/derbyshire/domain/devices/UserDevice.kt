package uk.derbyshire.domain.devices

import kotlin.uuid.Uuid

data class UserDevice(
    val deviceId: Uuid,
    val deviceNickname: String,
    val modelName: String,
    private val landscapeWidthPx: Int,
    private val landscapeHeightPx: Int,
    val orientation: Orientation,
    val colourSwatch: List<HexColour>?,
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