package uk.derbyshire.services

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.DrawingRepository
import uk.derbyshire.domain.drawings.DrawingId
import uk.derbyshire.domain.drawings.DrawingMetadata
import uk.derbyshire.domain.drawings.SaveDrawingFailure
import uk.derbyshire.domain.users.UserId

class DrawingService(
    private val imageProcessingService: ImageProcessingService,
    private val drawingRepository: DrawingRepository,
    private val context: DatabaseContext,
) {
    fun saveDrawing(userId: UserId, uploadedBytes: ByteArray): Result4k<DrawingId, SaveDrawingFailure> {
        val drawing = imageProcessingService.canonicaliseToPng(uploadedBytes).onFailure { return it }

        return context.transaction {
            drawingRepository.saveDrawing(userId, width = drawing.widthPx, height = drawing.heightPx, pngData = drawing.data)
        }.asSuccess()
    }

    fun getDrawingsForUser(userId: UserId): List<DrawingMetadata> =
        context.transaction {
            drawingRepository.getDrawingsForUser(userId)
        }

    fun getDrawingForUser(userId: UserId, drawingId: DrawingId): ByteArray? =
        context.transaction {
            drawingRepository.getDrawingData(userId, drawingId)
        }
}