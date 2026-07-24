package uk.derbyshire.domain.devices

import kotlin.uuid.Uuid

data class DeviceModel(
    val deviceModelId: Uuid,
    val modelName: String,
    val landscapeWidthPx: Int,
    val landscapeHeightPx: Int,
    val colourSwatch: List<HexColour>?,
)