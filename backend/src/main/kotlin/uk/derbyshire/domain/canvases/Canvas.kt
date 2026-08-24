package uk.derbyshire.domain.canvases

import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.Dimensions
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.devices.Palette
import kotlin.time.Instant

data class Canvas(
    val canvasId: CanvasId,
    val deviceModelId: DeviceModelId,
    override val landscapeWidthPx: Int,
    override val landscapeHeightPx: Int,
    override val orientation: Orientation,
    val status: CanvasStatus,
    val type: CanvasType,
    val palette: Palette?,
    val createdAt: Instant,
) : Dimensions