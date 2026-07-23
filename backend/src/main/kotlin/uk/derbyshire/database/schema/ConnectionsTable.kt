package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object ConnectionsTable : UuidTable("connections") {
    val senderUserId = reference("user_id", UserTable.id, ReferenceOption.CASCADE)
    val recipientUserId = reference("recipient_id", UserTable.id, ReferenceOption.CASCADE)

    val createdBy = reference("created_by", UserTable.id)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex(
            customIndexName = "connections_sender_recipient_unique",
            senderUserId,
            recipientUserId,
        )

        check("no_self_connections") {
            (senderUserId neq recipientUserId)
        }
    }
}