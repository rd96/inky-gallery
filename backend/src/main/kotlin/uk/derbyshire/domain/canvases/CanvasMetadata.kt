package uk.derbyshire.domain.canvases

import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.Dimensions
import uk.derbyshire.domain.devices.HexColour
import uk.derbyshire.domain.devices.Orientation
import kotlin.time.Instant

data class CanvasMetadata(
    val canvasId: CanvasId,
    val deviceModelId: DeviceModelId,
    override val landscapeWidthPx: Int,
    override val landscapeHeightPx: Int,
    override val orientation: Orientation,
    val status: CanvasStatus,
    val type: CanvasType,
    val colourSwatch: List<HexColour>?,
    val createdAt: Instant,
) : Dimensions