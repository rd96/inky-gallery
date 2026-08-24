package uk.derbyshire.domain.canvases

import uk.derbyshire.domain.drawings.Drawing

data class CanvasWithDrawings(
    val canvas: Canvas,
    val drawings: List<Drawing>,
)