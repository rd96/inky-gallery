package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.jdbc.insertIgnoreAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import uk.derbyshire.database.schema.DeviceModelTable
import uk.derbyshire.domain.devices.DeviceModel
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.HexColour

class DeviceModelRepository {
    fun insertModel(modelName: String, landscapeWidthPx: Int, landscapeHeightPx: Int, colourSwatch: List<HexColour>?): DeviceModelId? =
        DeviceModelTable.insertIgnoreAndGetId {
            it[this.modelName] = modelName
            it[this.landscapeWidthPx] = landscapeWidthPx
            it[this.landscapeHeightPx] = landscapeHeightPx
            it[this.colourSwatch] = colourSwatch?.map(HexColour::toString)
        }?.let { DeviceModelId(it.value) }

    fun getDeviceModels(): List<DeviceModel> =
        DeviceModelTable.select(
            DeviceModelTable.id,
            DeviceModelTable.modelName,
            DeviceModelTable.landscapeWidthPx,
            DeviceModelTable.landscapeHeightPx,
            DeviceModelTable.colourSwatch,
        ).map {
            DeviceModel(
                deviceModelId = DeviceModelId(it[DeviceModelTable.id].value),
                modelName = it[DeviceModelTable.modelName],
                landscapeWidthPx = it[DeviceModelTable.landscapeWidthPx],
                landscapeHeightPx = it[DeviceModelTable.landscapeHeightPx],
                colourSwatch = it[DeviceModelTable.colourSwatch]?.map(HexColour::parse),
            )
        }
}