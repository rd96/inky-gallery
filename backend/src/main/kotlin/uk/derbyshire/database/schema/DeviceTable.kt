package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.services.DeviceService.Companion.MAX_DEVICE_NICKNAME_LENGTH

object DeviceTable : UuidTable("devices") {
    val userId = reference("user_id", UserTable.id, ReferenceOption.CASCADE).index()
    val deviceModelId = reference("device_model_id", DeviceModelTable)
    val deviceNickname = varchar("device_nickname", MAX_DEVICE_NICKNAME_LENGTH)
    val orientation = enumerationByName<Orientation>("orientation", 20)
    val enabled = bool("enabled").default(true)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        index(
            customIndexName = "unique_user_id_device_nickname",
            isUnique = true,
            userId,
            deviceNickname,
        )
    }
}