package uk.derbyshire.domain.devices

interface Dimensions {
    val landscapeWidthPx: Int
    val landscapeHeightPx: Int
    val orientation: Orientation

    val widthPx: Int
        get() = when (orientation) {
            Orientation.LANDSCAPE -> landscapeWidthPx
            Orientation.PORTRAIT -> landscapeHeightPx
        }

    val heightPx: Int
        get() = when (orientation) {
            Orientation.LANDSCAPE -> landscapeHeightPx
            Orientation.PORTRAIT -> landscapeWidthPx
        }
}