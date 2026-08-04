package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import uk.derbyshire.services.DeviceService.Companion.MAX_DEVICE_MODEL_NAME_LENGTH

object DeviceModelTable : UuidTable("device_model") {
    val modelName = varchar("device_name", MAX_DEVICE_MODEL_NAME_LENGTH).uniqueIndex()
    val landscapeWidthPx = integer("landscape_width_px")
    val landscapeHeightPx = integer("landscape_height_px")
    val palette = array<String>("palette").nullable()
}