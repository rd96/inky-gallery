package uk.derbyshire.domain.canvases

import uk.derbyshire.domain.drawings.DrawingMetadata

data class CanvasDetail(
    val canvasMetadata: CanvasMetadata,
    val drawings: List<DrawingMetadata>,
)