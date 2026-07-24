package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.jdbc.insertIgnoreAndGetId
import uk.derbyshire.database.schema.DeviceTable
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.users.UserId
import kotlin.uuid.Uuid

class DeviceRepository {
    fun insertDevice(deviceModelId: Uuid, userId: UserId, deviceNickname: String, orientation: Orientation) =
        DeviceTable.insertIgnoreAndGetId {
            it[this.id] = deviceModelId
            it[this.userId] = userId.value
            it[this.deviceNickname] = deviceNickname
            it[this.orientation] = orientation
        }?.value

}