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
import uk.derbyshire.database.schema.ConnectionsTable
import uk.derbyshire.database.schema.UserTable
import uk.derbyshire.domain.connections.UserConnection
import uk.derbyshire.domain.users.UserId
import kotlin.uuid.Uuid

class ConnectionsRepository {
    fun insertConnection(senderUserId: UserId, recipientUserId: UserId, createdBy: UserId) =
        ConnectionsTable.insertIgnoreAndGetId {
                it[this.senderUserId] = senderUserId.value
                it[this.recipientUserId] = recipientUserId.value
                it[this.createdBy] = createdBy.value
            }?.value

    fun deleteConnection(connectionId: Uuid) = transaction {
        ConnectionsTable.deleteWhere { ConnectionsTable.id eq connectionId }
    }

    private fun getConnectionsForUser(userId: UserId, ownerColumn: Column<EntityID<Uuid>>, connectedUserColumn: Column<EntityID<Uuid>>, onlyEnabled: Boolean = false): List<UserConnection> =
        ConnectionsTable
            .innerJoin(UserTable) {
                connectedUserColumn eq UserTable.id
            }
            .select(
                ConnectionsTable.id,
                UserTable.id,
                UserTable.username,
                UserTable.displayName,
                ConnectionsTable.createdAt,
                UserTable.enabled,
            )
            .where {
                (ownerColumn eq userId.value) and
                    (if (onlyEnabled) UserTable.enabled else Op.TRUE)
            }
            .map {
                UserConnection(
                    connectionId = it[ConnectionsTable.id].value,
                    userId = UserId(it[UserTable.id].value),
                    username = it[UserTable.username],
                    displayName = it[UserTable.displayName],
                    createdAt = it[ConnectionsTable.createdAt],
                    enabled = it[UserTable.enabled],
                )
            }

    fun getRecipientsFor(userId: UserId, onlyEnabled: Boolean): List<UserConnection> =
        getConnectionsForUser(
            userId = userId,
            ownerColumn = ConnectionsTable.senderUserId,
            connectedUserColumn = ConnectionsTable.recipientUserId,
            onlyEnabled = onlyEnabled,
        )

    fun getSendersFor(userId: UserId, onlyEnabled: Boolean): List<UserConnection> =
        getConnectionsForUser(
            userId = userId,
            ownerColumn = ConnectionsTable.recipientUserId,
            connectedUserColumn = ConnectionsTable.senderUserId,
            onlyEnabled = onlyEnabled,
        )

}