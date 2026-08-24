package uk.derbyshire.domain.messages

import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.Dimensions
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.users.UserId
import kotlin.time.Instant

data class CanvasMessage(
    val messageId: MessageId,
    val fromUserId: UserId,
    val fromDisplayName: String,
    val fromUsername: String,
    val message: String?,
    val showName: Boolean,
    val sentAt: Instant,
    val canvasId: CanvasId,
    val targetDeviceModelId: DeviceModelId,
    override val orientation: Orientation,
    override val landscapeWidthPx: Int,
    override val landscapeHeightPx: Int,
): Dimensions