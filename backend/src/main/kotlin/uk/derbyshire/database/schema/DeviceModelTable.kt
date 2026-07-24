package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object DeviceModelTable : UuidTable("device_model") {
    val modelName = varchar("device_name", 50).uniqueIndex()
    val landscapeWidthPx = integer("landscape_width_px")
    val landscapeHeightPx = integer("landscape_height_px")
    val colourSwatch = array<String>("colour_swatch").nullable()
}