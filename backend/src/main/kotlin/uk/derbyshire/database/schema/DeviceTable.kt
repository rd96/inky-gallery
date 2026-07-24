package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import uk.derbyshire.domain.devices.Orientation

object DeviceTable : UuidTable("devices") {
    val userId = reference("user_id", UserTable.id, ReferenceOption.CASCADE).index()
    val deviceModelId = reference("device_model_id", DeviceModelTable)
    val deviceNickname = varchar("device_nickname", 50)
    val orientation = enumerationByName<Orientation>("orientation", 20).default(Orientation.PORTRAIT)
    val enabled = bool("enabled").default(true)
}