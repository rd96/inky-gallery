package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object DrawingTable : UuidTable("drawings") {
    val canvasId = reference("canvas_id", CanvasTable, ReferenceOption.CASCADE)

    val byteSize = integer("byte_size").check { it greater 0 }
    val pngData = binary("png_data")

    val position = integer("position").check { it greaterEq 0 }

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        index(
            customIndexName = "unique_canvas_id_position",
            isUnique = true,
            canvasId,
            position,
        )
    }
}