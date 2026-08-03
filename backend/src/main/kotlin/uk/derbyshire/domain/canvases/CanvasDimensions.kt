package uk.derbyshire.domain.canvases

import uk.derbyshire.domain.devices.Dimensions
import uk.derbyshire.domain.devices.Orientation

data class CanvasDimensions(
    val canvasId: CanvasId,
    override val landscapeWidthPx: Int,
    override val landscapeHeightPx: Int,
    override val orientation: Orientation,
    val status: CanvasStatus,
    val type: CanvasType,
    val drawingsCount: Int,
) : Dimensions