package uk.derbyshire.domain.drawings

import kotlin.time.Instant

data class Drawing(
    val drawingId: DrawingId,
    val position: Int,
    val createdAt: Instant,
)