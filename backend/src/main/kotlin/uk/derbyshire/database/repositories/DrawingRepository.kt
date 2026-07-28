package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import uk.derbyshire.database.schema.DrawingTable
import uk.derbyshire.domain.drawings.DrawingMetadata
import uk.derbyshire.domain.users.UserId
import kotlin.uuid.Uuid

class DrawingRepository {
    fun saveDrawing(userId: UserId, width: Int, height: Int, pngData: ByteArray): Uuid =
        DrawingTable.insertAndGetId {
            it[this.userId] = userId.value
            it[this.widthPx] = width
            it[this.heightPx] = height
            it[this.pngData] = pngData
            it[this.byteSize] = pngData.size
        }.value

    fun getDrawingsForUser(userId: UserId): List<DrawingMetadata> =
        DrawingTable
            .select(
                DrawingTable.id,
                DrawingTable.widthPx,
                DrawingTable.heightPx,
                DrawingTable.createdAt,
            )
            .where { DrawingTable.userId eq userId.value }
            .orderBy(DrawingTable.createdAt to SortOrder.DESC)
            .map {
                DrawingMetadata(
                    drawingId = it[DrawingTable.id].value,
                    widthPx = it[DrawingTable.widthPx],
                    heightPx = it[DrawingTable.heightPx],
                    createdAt = it[DrawingTable.createdAt],
                )
            }

    fun getDrawingData(userId: UserId, drawingId: Uuid) =
        DrawingTable
            .select(
                DrawingTable.pngData,
            )
            .where { (DrawingTable.userId eq userId.value) and (DrawingTable.id eq drawingId) }
            .singleOrNull()
            ?.let {
                it[DrawingTable.pngData]
            }
}