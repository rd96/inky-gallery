package uk.derbyshire.api.me.shared

import uk.derbyshire.domain.drawings.DrawingId
import uk.derbyshire.domain.drawings.DrawingMetadata
import kotlin.time.Instant

data class DrawingDTO(
    val drawingId: DrawingId,
    val position: Int,
    val createdAt: Instant,
) {
    companion object {
        fun List<DrawingMetadata>.toDto() = map {
            DrawingDTO(
                drawingId = it.drawingId,
                position = it.position,
                createdAt = it.createdAt,
            )
        }
    }
}