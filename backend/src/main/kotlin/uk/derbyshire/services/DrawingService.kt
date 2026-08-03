package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.CanvasRepository
import uk.derbyshire.database.repositories.DrawingRepository
import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.drawings.DrawingId
import uk.derbyshire.domain.drawings.SaveDrawingFailure
import uk.derbyshire.domain.users.UserId

class DrawingService(
    private val imageProcessingService: ImageProcessingService,
    private val drawingRepository: DrawingRepository,
    private val canvasRepository: CanvasRepository,
    private val context: DatabaseContext,
) {

    fun saveDrawing(userId: UserId, canvasId: CanvasId, uploadedBytes: ByteArray): Result4k<DrawingId, SaveDrawingFailure> =
        context.transaction {
            val canvas = canvasRepository.getCanvasDimensionsAndDrawings(userId, canvasId) ?: return@transaction Failure(SaveDrawingFailure.CANVAS_NOT_FOUND)

            val drawing = imageProcessingService.canonicaliseToPng(uploadedBytes).onFailure { return@transaction it }

            // check dimensions match canvas

            context.transaction {
                drawingRepository.saveDrawing(canvasId, canvas.drawingsCount, pngData = drawing.data)
            }.asSuccess()
        }

    fun getDrawingForUser(userId: UserId, canvasId: CanvasId, drawingId: DrawingId): ByteArray? =
        // we actually need to check here whether the user can access the drawing, either if they own it or have received it
        context.transaction {
            drawingRepository.getDrawingData(canvasId, drawingId)
        }
}