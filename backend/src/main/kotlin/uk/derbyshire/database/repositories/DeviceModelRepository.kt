package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertIgnoreAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import uk.derbyshire.database.schema.DeviceModelTable
import uk.derbyshire.domain.devices.DeviceModel
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.HexColour

class DeviceModelRepository {
    fun insertModel(modelName: String, landscapeWidthPx: Int, landscapeHeightPx: Int, palette: List<HexColour>?): DeviceModelId? =
        DeviceModelTable.insertIgnoreAndGetId {
            it[this.modelName] = modelName
            it[this.landscapeWidthPx] = landscapeWidthPx
            it[this.landscapeHeightPx] = landscapeHeightPx
            it[this.palette] = palette?.map(HexColour::toString)
        }?.let { DeviceModelId(it.value) }

    fun getDeviceModels(): List<DeviceModel> =
        DeviceModelTable.select(
            DeviceModelTable.id,
            DeviceModelTable.modelName,
            DeviceModelTable.landscapeWidthPx,
            DeviceModelTable.landscapeHeightPx,
            DeviceModelTable.palette,
        ).map {
            DeviceModel(
                deviceModelId = DeviceModelId(it[DeviceModelTable.id].value),
                modelName = it[DeviceModelTable.modelName],
                landscapeWidthPx = it[DeviceModelTable.landscapeWidthPx],
                landscapeHeightPx = it[DeviceModelTable.landscapeHeightPx],
                palette = it[DeviceModelTable.palette]?.map(HexColour::parse),
            )
        }

    fun modelExists(deviceModelId: DeviceModelId): Boolean =
        DeviceModelTable
            .select(DeviceModelTable.id)
            .where { DeviceModelTable.id eq deviceModelId.value }
            .any()
}