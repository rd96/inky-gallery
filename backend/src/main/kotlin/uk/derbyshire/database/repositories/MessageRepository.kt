package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import uk.derbyshire.database.schema.CanvasTable
import uk.derbyshire.database.schema.DeviceModelTable
import uk.derbyshire.database.schema.MessageTable
import uk.derbyshire.database.schema.UserTable
import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.messages.CanvasMessage
import uk.derbyshire.domain.messages.MessageId
import uk.derbyshire.domain.users.UserId

class MessageRepository {
    fun insertMessage(fromUserId: UserId, toUserId: UserId, canvasId: CanvasId, message: String?, showName: Boolean) {
        MessageTable.insert {
            it[this.fromUserId] = fromUserId.value
            it[this.toUserId] = toUserId.value
            it[this.canvasId] = canvasId.value
            it[this.message] = message
            it[this.showName] = showName
        }
    }

    fun getMessagesFor(recipientUserId: UserId): List<CanvasMessage> =
        MessageTable
            .join(UserTable, JoinType.INNER, MessageTable.fromUserId, UserTable.id)
            .join(CanvasTable, JoinType.INNER, MessageTable.canvasId, CanvasTable.id)
            .join(DeviceModelTable, JoinType.INNER, CanvasTable.targetDeviceModelId, DeviceModelTable.id)
        .select(
            MessageTable.id,
            MessageTable.fromUserId,
            UserTable.displayName,
            UserTable.username,
            MessageTable.message,
            MessageTable.showName,
            MessageTable.sentAt,
            MessageTable.canvasId,
            CanvasTable.targetDeviceModelId,
            CanvasTable.orientation,
            DeviceModelTable.landscapeWidthPx,
            DeviceModelTable.landscapeHeightPx,
        )
            .where { MessageTable.toUserId eq recipientUserId.value }
            .orderBy(MessageTable.sentAt to SortOrder.DESC)
            .map {
                CanvasMessage(
                    messageId = MessageId(it[MessageTable.id].value),
                    fromUserId = UserId(it[MessageTable.fromUserId].value),
                    fromDisplayName = it[UserTable.displayName],
                    fromUsername = it[UserTable.username],
                    message = it[MessageTable.message],
                    showName = it[MessageTable.showName],
                    sentAt = it[MessageTable.sentAt],
                    canvasId = CanvasId(it[MessageTable.canvasId].value),
                    targetDeviceModelId = DeviceModelId(it[CanvasTable.targetDeviceModelId].value),
                    orientation = it[CanvasTable.orientation],
                    landscapeWidthPx = it[DeviceModelTable.landscapeWidthPx],
                    landscapeHeightPx = it[DeviceModelTable.landscapeHeightPx],
                )
            }
}