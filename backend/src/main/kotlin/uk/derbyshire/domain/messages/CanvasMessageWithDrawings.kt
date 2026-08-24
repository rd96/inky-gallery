package uk.derbyshire.domain.messages

import uk.derbyshire.domain.drawings.Drawing

data class CanvasMessageWithDrawings(
    val canvasMessage: CanvasMessage,
    val drawings: List<Drawing>,
)