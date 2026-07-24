package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertIgnoreAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import uk.derbyshire.database.schema.ConnectionTable
import uk.derbyshire.database.schema.UserTable
import uk.derbyshire.domain.connections.UserConnection
import uk.derbyshire.domain.users.UserId
import kotlin.uuid.Uuid

class ConnectionRepository {
    fun insertConnection(senderUserId: UserId, recipientUserId: UserId, createdBy: UserId) =
        ConnectionTable.insertIgnoreAndGetId {
                it[this.senderUserId] = senderUserId.value
                it[this.recipientUserId] = recipientUserId.value
                it[this.createdBy] = createdBy.value
            }?.value

    fun deleteConnection(connectionId: Uuid) = transaction {
        ConnectionTable.deleteWhere { ConnectionTable.id eq connectionId }
    }

    private fun getConnectionsForUser(userId: UserId, ownerColumn: Column<EntityID<Uuid>>, connectedUserColumn: Column<EntityID<Uuid>>, onlyEnabled: Boolean = false): List<UserConnection> =
        ConnectionTable
            .innerJoin(UserTable) {
                connectedUserColumn eq UserTable.id
            }
            .select(
                ConnectionTable.id,
                UserTable.id,
                UserTable.username,
                UserTable.displayName,
                ConnectionTable.createdAt,
                UserTable.enabled,
            )
            .where {
                (ownerColumn eq userId.value) and
                    (if (onlyEnabled) UserTable.enabled else Op.TRUE)
            }
            .map {
                UserConnection(
                    connectionId = it[ConnectionTable.id].value,
                    userId = UserId(it[UserTable.id].value),
                    username = it[UserTable.username],
                    displayName = it[UserTable.displayName],
                    createdAt = it[ConnectionTable.createdAt],
                    enabled = it[UserTable.enabled],
                )
            }

    fun getRecipientsFor(userId: UserId, onlyEnabled: Boolean): List<UserConnection> =
        getConnectionsForUser(
            userId = userId,
            ownerColumn = ConnectionTable.senderUserId,
            connectedUserColumn = ConnectionTable.recipientUserId,
            onlyEnabled = onlyEnabled,
        )

    fun getSendersFor(userId: UserId, onlyEnabled: Boolean): List<UserConnection> =
        getConnectionsForUser(
            userId = userId,
            ownerColumn = ConnectionTable.recipientUserId,
            connectedUserColumn = ConnectionTable.senderUserId,
            onlyEnabled = onlyEnabled,
        )

}