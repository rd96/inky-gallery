package uk.derbyshire.domain.drawings

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class DrawingMetadata(
    val drawingId: Uuid,
    val widthPx: Int,
    val heightPx: Int,
    val createdAt: Instant,
)