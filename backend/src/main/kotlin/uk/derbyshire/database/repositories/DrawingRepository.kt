package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.core.Case
import org.jetbrains.exposed.v1.core.CaseWhen
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.intParam
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import uk.derbyshire.database.schema.CanvasTable
import uk.derbyshire.database.schema.DrawingTable
import uk.derbyshire.database.schema.MessageTable
import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.drawings.DrawingId
import uk.derbyshire.domain.drawings.Drawing
import uk.derbyshire.domain.messages.MessageId
import uk.derbyshire.domain.users.UserId

class DrawingRepository {
    fun saveDrawing(canvasId: CanvasId, position: Int, pngData: ByteArray): DrawingId =
        DrawingTable.insertAndGetId {
            it[this.canvasId] = canvasId.value
            it[this.position] = position
            it[this.pngData] = pngData
            it[this.byteSize] = pngData.size
        }.let { DrawingId(it.value) }

    fun getDrawingsByCanvasIds(canvasIds: List<CanvasId>): Map<CanvasId, List<Drawing>> =
        DrawingTable
            .select(
                DrawingTable.id,
                DrawingTable.canvasId,
                DrawingTable.position,
                DrawingTable.createdAt,
            )
            .where { DrawingTable.canvasId inList canvasIds.map(CanvasId::value) }
            .orderBy(DrawingTable.position to SortOrder.ASC)
            .groupBy { CanvasId(it[DrawingTable.canvasId].value) }
            .mapValues { (_, rows) ->
                rows.map {
                    Drawing(
                        drawingId = DrawingId(it[DrawingTable.id].value),
                        position = it[DrawingTable.position],
                        createdAt = it[DrawingTable.createdAt],
                    )
                }
            }

    fun getDrawingIdsByCanvasId(canvasId: CanvasId): List<DrawingId> =
        DrawingTable
            .select(DrawingTable.id)
            .where { DrawingTable.canvasId eq canvasId.value }
            .orderBy(DrawingTable.position to SortOrder.ASC)
            .map { DrawingId(it[DrawingTable.id].value) }

    fun getDrawingData(userId: UserId, canvasId: CanvasId, drawingId: DrawingId): ByteArray? =
        DrawingTable
            .innerJoin(CanvasTable)
            .select(
                DrawingTable.pngData,
            )
            .where { (CanvasTable.id eq canvasId.value) and (CanvasTable.createdBy eq userId.value) and (DrawingTable.id eq drawingId.value) }
            .singleOrNull()
            ?.let {
                it[DrawingTable.pngData]
            }

    fun getDrawingData(userId: UserId, messageId: MessageId, drawingId: DrawingId): ByteArray? =
        MessageTable
            .innerJoin(CanvasTable)
            .innerJoin(DrawingTable)
            .select(
                DrawingTable.pngData,
            )
            .where { (MessageTable.id eq messageId.value) and (MessageTable.toUserId eq userId.value) and (DrawingTable.id eq drawingId.value) }
            .singleOrNull()
            ?.let {
                it[DrawingTable.pngData]
            }

    fun updateDrawingPositions(canvasId: CanvasId, orderedDrawingIds: List<DrawingId>): Int =
        DrawingTable.update({
            (DrawingTable.canvasId eq canvasId.value) and (DrawingTable.id inList orderedDrawingIds.map(DrawingId::value))
        }) {
            it[DrawingTable.position] = orderedDrawingIds.withIndex()
                .fold(CaseWhen<Int>()) { case, (position, drawingId) -> case.When(DrawingTable.id eq drawingId.value, intParam(position)) }
                .Else(DrawingTable.position)
        }
}