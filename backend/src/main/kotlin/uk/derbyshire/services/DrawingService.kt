package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.onFailure
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.CanvasRepository
import uk.derbyshire.database.repositories.DrawingRepository
import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.canvases.CanvasStatus
import uk.derbyshire.domain.canvases.CanvasType
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
            val canvas = canvasRepository.getCanvasWithDrawingCount(userId, canvasId) ?: return@transaction Failure(SaveDrawingFailure.CANVAS_NOT_FOUND)

            if (canvas.status != CanvasStatus.DRAFT) return@transaction Failure(SaveDrawingFailure.CANVAS_IS_NOT_IN_DRAFT)

            if (canvas.type == CanvasType.SINGLE && canvas.drawingsCount >= 1) return@transaction Failure(SaveDrawingFailure.SINGLE_CANVAS_CAN_ONLY_HAVE_ONE_DRAWING)

            val drawing = imageProcessingService.canonicaliseToPng(uploadedBytes, canvas.widthPx, canvas.heightPx).onFailure { return@transaction it }

            if (canvas.type == CanvasType.SINGLE) {
                canvasRepository.updateCanvasStatus(userId, canvasId, CanvasStatus.FINISHED)
                    .mapFailure { SaveDrawingFailure.CANVAS_NOT_FOUND }
            }

            drawingRepository.saveDrawing(canvasId, canvas.drawingsCount, pngData = drawing.data)
                .asSuccess()
        }

    fun getDrawingForUser(userId: UserId, canvasId: CanvasId, drawingId: DrawingId): ByteArray? =
        context.transaction {
            drawingRepository.getDrawingData(userId, canvasId, drawingId)
        }
}