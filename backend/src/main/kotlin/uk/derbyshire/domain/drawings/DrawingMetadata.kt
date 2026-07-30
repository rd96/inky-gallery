package uk.derbyshire.domain.drawings

import kotlin.time.Instant

data class DrawingMetadata(
    val drawingId: DrawingId,
    val widthPx: Int,
    val heightPx: Int,
    val createdAt: Instant,
)