package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import uk.derbyshire.services.MessageService.Companion.MAX_MESSAGE_LENGTH

object MessageTable : UuidTable("messages") {
    val fromUserId = reference("from_user_id", UserTable.id)
    val toUserId = reference("to_user_id", UserTable.id).index()

    val canvasId = reference("canvas_id", CanvasTable.id)
    val message = varchar("message", MAX_MESSAGE_LENGTH).nullable()
    val showName = bool("show_name")

    val sentAt = timestamp("sent_at").defaultExpression(CurrentTimestamp)
    val readAt = timestamp("read_at").nullable()
}