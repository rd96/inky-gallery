package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.between
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import uk.derbyshire.domain.drawings.DrawingLimits.MAX_DIMENSION_PX
import uk.derbyshire.domain.drawings.DrawingLimits.MIN_DIMENSION_PX

object DrawingTable : UuidTable("drawings") {
    val userId = reference("user_id", UserTable, ReferenceOption.CASCADE)

    val widthPx = integer("width_px").check { it.between(MIN_DIMENSION_PX, MAX_DIMENSION_PX) }
    val heightPx = integer("height_px").check { it.between(MIN_DIMENSION_PX, MAX_DIMENSION_PX) }

    val byteSize = integer("byte_size").check { it greater 0 }
    val pngData = binary("png_data")

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}