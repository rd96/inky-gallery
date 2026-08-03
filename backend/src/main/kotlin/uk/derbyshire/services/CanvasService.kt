package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.CanvasRepository
import uk.derbyshire.database.repositories.DeviceModelRepository
import uk.derbyshire.database.repositories.DrawingRepository
import uk.derbyshire.domain.canvases.CanvasDetail
import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.canvases.CanvasMetadata
import uk.derbyshire.domain.canvases.CanvasStatus
import uk.derbyshire.domain.canvases.CanvasType
import uk.derbyshire.domain.canvases.CreateCanvasFailure
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.users.UserId

class CanvasService(
    private val canvasRepository: CanvasRepository,
    private val deviceModelRepository: DeviceModelRepository,
    private val drawingRepository: DrawingRepository,
    private val context: DatabaseContext,
) {
    fun createCanvas(targetDeviceModelId: DeviceModelId, orientation: Orientation, type: CanvasType, createdBy: UserId): Result4k<CanvasId, CreateCanvasFailure> =
        context.transaction {
            // check limits, max three pending at a time
            if (!deviceModelRepository.modelExists(targetDeviceModelId)) return@transaction Failure(CreateCanvasFailure.DEVICE_MODEL_NOT_FOUND)

            canvasRepository.insertCanvas(targetDeviceModelId, orientation, type, CanvasStatus.DRAFT, createdBy).asSuccess()
        }

    fun getMyCanvases(userId: UserId, canvasStatus: CanvasStatus?): List<CanvasDetail> =
        context.transaction {
            val canvases = canvasRepository.findUserCanvases(userId, canvasStatus)
            val canvasIds = canvases.map { it.canvasId }

            val drawings = drawingRepository.getDrawingsByCanvasIds(canvasIds)

            // TODO get can send to/sent recipients

            canvases.map {
                CanvasDetail(
                    canvasMetadata = it,
                    drawings = drawings[it.canvasId] ?: emptyList(),
                    sentTo = emptyList(),
                    canSendTo = emptyList(),
                )
            }
        }

    fun getMyCanvas(userId: UserId, canvasId: CanvasId): CanvasMetadata? =
        context.transaction {
            canvasRepository.getCanvas(userId, canvasId)
        }
}