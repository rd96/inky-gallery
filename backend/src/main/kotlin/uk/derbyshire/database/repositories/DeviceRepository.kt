package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.jdbc.insertIgnoreAndGetId
import uk.derbyshire.database.schema.DeviceTable
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.users.UserId
import kotlin.uuid.Uuid

class DeviceRepository {
    fun insertDevice(userId: UserId, deviceModelId: Uuid, deviceNickname: String, orientation: Orientation) =
        DeviceTable.insertIgnoreAndGetId {
            it[this.id] = deviceModelId
            it[this.userId] = userId.value
            it[this.deviceModelId] = deviceModelId
            it[this.deviceNickname] = deviceNickname
            it[this.orientation] = orientation
        }?.value

    fun updateDevice(userId: UserId, deviceId: Uuid, deviceNickname: String?, orientation: Orientation?, enabled: Boolean?) =
        try {
            val success = DeviceTable.update({ (DeviceTable.id eq deviceId) and (DeviceTable.userId eq userId.value) }) { table ->
                deviceNickname?.let { table[this.deviceNickname] = it }
                orientation?.let { table[this.orientation] = it }
                enabled?.let { table[this.enabled] = it }
            } == 1

            if (success) Success(Unit) else Failure(UpdateDeviceFailure.DEVICE_NOT_FOUND)
        } catch (e: ExposedSQLException) {
            if (e.sqlState == PSQLState.UNIQUE_VIOLATION.state) Failure(UpdateDeviceFailure.DEVICE_NICKNAME_IN_USE_FOR_USER)
            else throw e
        }

    fun getDevicesForUser(userId: UserId): List<UserDevice> =
        DeviceTable.innerJoin(DeviceModelTable)
            .select(
                DeviceTable.id,
                DeviceTable.deviceNickname,
                DeviceModelTable.modelName,
                DeviceModelTable.landscapeWidthPx,
                DeviceModelTable.landscapeHeightPx,
                DeviceTable.orientation,
                DeviceModelTable.colourSwatch,
            )
            .where { DeviceTable.userId eq userId.value }
            .map {
                UserDevice(
                    deviceId = it[DeviceTable.id].value,
                    deviceNickname = it[DeviceTable.deviceNickname],
                    modelName = it[DeviceModelTable.modelName],
                    landscapeWidthPx = it[DeviceModelTable.landscapeWidthPx],
                    landscapeHeightPx = it[DeviceModelTable.landscapeHeightPx],
                    orientation = it[DeviceTable.orientation],
                    colourSwatch = it[DeviceModelTable.colourSwatch]?.map(HexColour::parse)
                )
            }

}