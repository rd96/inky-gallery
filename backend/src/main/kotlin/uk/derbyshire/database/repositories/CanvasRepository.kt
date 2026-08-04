package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.core.Count
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.andIfNotNull
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import uk.derbyshire.database.schema.CanvasTable
import uk.derbyshire.database.schema.DeviceModelTable
import uk.derbyshire.database.schema.DrawingTable
import uk.derbyshire.domain.canvases.CanvasDimensions
import uk.derbyshire.domain.canvases.CanvasMetadata
import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.canvases.CanvasStatus
import uk.derbyshire.domain.canvases.CanvasType
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.HexColour
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.users.UserId

class CanvasRepository {
    fun insertCanvas(targetDeviceModelId: DeviceModelId, orientation: Orientation, type: CanvasType, status: CanvasStatus, createdBy: UserId): CanvasId =
        CanvasTable.insertAndGetId {
            it[this.targetDeviceModel] = targetDeviceModelId.value
            it[this.orientation] = orientation
            it[this.createdBy] = createdBy.value
            it[this.status] = status
            it[this.type] = type
        }.let { CanvasId(it.value) }

    fun findUserCanvases(userId: UserId, canvasStatus: CanvasStatus?): List<CanvasMetadata> =
        CanvasTable
            .innerJoin(DeviceModelTable)
            .select(
                CanvasTable.id,
                DeviceModelTable.id,
                CanvasTable.orientation,
                DeviceModelTable.landscapeWidthPx,
                DeviceModelTable.landscapeHeightPx,
                CanvasTable.status,
                CanvasTable.type,
                DeviceModelTable.palette,
                CanvasTable.createdAt,
            )
            .where {
                (CanvasTable.createdBy eq userId.value)
                    .andIfNotNull(canvasStatus?.let { CanvasTable.status eq it })}
            .orderBy(CanvasTable.createdAt, SortOrder.DESC)
            .map { it.toCanvasMetadata() }

    fun userOwnsCanvas(userId: UserId, canvasId: CanvasId): Boolean =
        CanvasTable
            .select(CanvasTable.id)
            .where { (CanvasTable.id eq canvasId.value) and (CanvasTable.createdBy eq userId.value) }
            .any()

    fun countDraftCanvases(userId: UserId): Long =
        CanvasTable
            .selectAll()
            .where { CanvasTable.createdBy eq userId.value }
            .count()

    fun getCanvas(userId: UserId, canvasId: CanvasId): CanvasMetadata? =
        CanvasTable
            .innerJoin(DeviceModelTable)
            .select(
                CanvasTable.id,
                DeviceModelTable.id,
                CanvasTable.orientation,
                DeviceModelTable.landscapeWidthPx,
                DeviceModelTable.landscapeHeightPx,
                CanvasTable.status,
                CanvasTable.type,
                DeviceModelTable.palette,
                CanvasTable.createdAt,
            )
            .where {
                (CanvasTable.id eq canvasId.value) and (CanvasTable.createdBy eq userId.value)
            }
            .singleOrNull()
            ?.toCanvasMetadata()

    fun getCanvasDimensionsAndDrawings(userId: UserId, canvasId: CanvasId): CanvasDimensions? =
        CanvasTable
            .innerJoin(DeviceModelTable)
            .leftJoin(DrawingTable)
            .select(
                CanvasTable.id,
                CanvasTable.orientation,
                DeviceModelTable.landscapeWidthPx,
                DeviceModelTable.landscapeHeightPx,
                CanvasTable.status,
                CanvasTable.type,
                Count(DrawingTable.id)
            )
            .where { (CanvasTable.id eq canvasId.value) and (CanvasTable.createdBy eq userId.value) }
            .groupBy(
                CanvasTable.id,
                CanvasTable.orientation,
                DeviceModelTable.modelName,
                DeviceModelTable.landscapeWidthPx,
                DeviceModelTable.landscapeHeightPx,
                CanvasTable.status,
                CanvasTable.type,
            )
            .singleOrNull()
            ?.let {
                CanvasDimensions(
                    canvasId = CanvasId(canvasId.value),
                    landscapeWidthPx = it[DeviceModelTable.landscapeWidthPx],
                    landscapeHeightPx = it[DeviceModelTable.landscapeHeightPx],
                    orientation = it[CanvasTable.orientation],
                    status = it[CanvasTable.status],
                    type = it[CanvasTable.type],
                    drawingsCount = it[Count(DrawingTable.id)].toInt(),
                )
            }

    companion object {
        private fun ResultRow.toCanvasMetadata() = CanvasMetadata(
            canvasId = CanvasId(this[CanvasTable.id].value),
            deviceModelId = DeviceModelId(this[DeviceModelTable.id].value),
            landscapeWidthPx = this[DeviceModelTable.landscapeWidthPx],
            landscapeHeightPx = this[DeviceModelTable.landscapeHeightPx],
            orientation = this[CanvasTable.orientation],
            status = this[CanvasTable.status],
            type = this[CanvasTable.type],
            palette = this[DeviceModelTable.palette]?.map(HexColour::parse),
            createdAt = this[CanvasTable.createdAt],
        )
    }
}