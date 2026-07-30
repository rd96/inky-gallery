package uk.derbyshire.database.repositories

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.insertIgnoreAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.postgresql.util.PSQLState
import uk.derbyshire.database.schema.DeviceModelTable
import uk.derbyshire.database.schema.DeviceTable
import uk.derbyshire.domain.devices.DeviceId
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.HexColour
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.devices.UpdateDeviceFailure
import uk.derbyshire.domain.devices.UserDevice
import uk.derbyshire.domain.users.UserId

class DeviceRepository {
    fun insertDevice(userId: UserId, deviceModelId: DeviceModelId, deviceNickname: String, orientation: Orientation): DeviceId? =
        DeviceTable.insertIgnoreAndGetId {
            it[this.userId] = userId.value
            it[this.deviceModelId] = deviceModelId.value
            it[this.deviceNickname] = deviceNickname
            it[this.orientation] = orientation
        }?.let { DeviceId(it.value) }

    fun updateDevice(userId: UserId, deviceId: DeviceId, deviceNickname: String?, orientation: Orientation?, enabled: Boolean?) =
        try {
            val success = DeviceTable.update({ (DeviceTable.id eq deviceId.value) and (DeviceTable.userId eq userId.value) }) { table ->
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
                DeviceTable.enabled,
            )
            .where { DeviceTable.userId eq userId.value }
            .orderBy(DeviceTable.createdAt, SortOrder.ASC)
            .map {
                UserDevice(
                    deviceId = DeviceId(it[DeviceTable.id].value),
                    deviceNickname = it[DeviceTable.deviceNickname],
                    modelName = it[DeviceModelTable.modelName],
                    landscapeWidthPx = it[DeviceModelTable.landscapeWidthPx],
                    landscapeHeightPx = it[DeviceModelTable.landscapeHeightPx],
                    orientation = it[DeviceTable.orientation],
                    colourSwatch = it[DeviceModelTable.colourSwatch]?.map(HexColour::parse),
                    enabled = it[DeviceTable.enabled],
                )
            }

}