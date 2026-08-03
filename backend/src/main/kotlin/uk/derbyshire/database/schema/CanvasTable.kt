package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import uk.derbyshire.domain.canvases.CanvasStatus
import uk.derbyshire.domain.canvases.CanvasType
import uk.derbyshire.domain.devices.Orientation

object CanvasTable : UuidTable("canvases") {
    val targetDeviceModel = reference("target_device_id", DeviceModelTable, ReferenceOption.CASCADE)
    val orientation = enumerationByName<Orientation>("target_orientation", 20)

    val status = enumerationByName<CanvasStatus>("canvas_status", 20)
    val type = enumerationByName<CanvasType>("canvas_type", 20)

    val createdBy = reference("created_by", UserTable, ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}