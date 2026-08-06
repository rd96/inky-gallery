package uk.derbyshire.domain.drawings

import kotlin.time.Instant

data class DrawingMetadata(
    val drawingId: DrawingId,
    val position: Int,
    val createdAt: Instant,
)