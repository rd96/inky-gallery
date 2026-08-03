package uk.derbyshire.domain.canvases

import uk.derbyshire.domain.drawings.DrawingMetadata
import uk.derbyshire.domain.users.UserId

data class CanvasDetail(
    val canvasMetadata: CanvasMetadata,
    val drawings: List<DrawingMetadata>,
    val sentTo: List<UserId>,
    val canSendTo: List<UserId>,
)