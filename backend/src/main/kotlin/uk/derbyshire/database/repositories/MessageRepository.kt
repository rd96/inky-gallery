package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.jdbc.insert
import uk.derbyshire.database.schema.MessageTable
import uk.derbyshire.domain.canvases.CanvasId
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
}