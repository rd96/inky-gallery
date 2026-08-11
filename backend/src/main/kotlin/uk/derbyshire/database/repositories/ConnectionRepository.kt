package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.andIfNotNull
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertIgnoreAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import uk.derbyshire.database.schema.ConnectionTable
import uk.derbyshire.database.schema.DeviceTable
import uk.derbyshire.database.schema.UserTable
import uk.derbyshire.domain.connections.ConnectionId
import uk.derbyshire.domain.connections.UserConnection
import uk.derbyshire.domain.devices.DeviceModelId
import uk.derbyshire.domain.devices.Orientation
import uk.derbyshire.domain.users.UserId
import kotlin.uuid.Uuid

class ConnectionRepository {
    fun insertConnection(senderUserId: UserId, recipientUserId: UserId, createdBy: UserId): ConnectionId? =
        ConnectionTable.insertIgnoreAndGetId {
                it[this.senderUserId] = senderUserId.value
                it[this.recipientUserId] = recipientUserId.value
                it[this.createdBy] = createdBy.value
            }?.let { ConnectionId(it.value) }

    fun deleteConnection(connectionId: ConnectionId) =
        ConnectionTable.deleteWhere { ConnectionTable.id eq connectionId.value }

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
                    connectionId = ConnectionId(it[ConnectionTable.id].value),
                    userId = UserId(it[UserTable.id].value),
                    username = it[UserTable.username],
                    displayName = it[UserTable.displayName],
                    createdAt = it[ConnectionTable.createdAt],
                    userEnabled = it[UserTable.enabled],
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

    fun searchRecipientsByDevicesFor(
        userId: UserId,
        deviceModelId: DeviceModelId?,
        deviceOrientation: Orientation?,
    ): List<UserConnection> =
        ConnectionTable
            .join(UserTable, JoinType.INNER, UserTable.id, ConnectionTable.recipientUserId)
            .join(DeviceTable, JoinType.INNER, DeviceTable.userId, ConnectionTable.recipientUserId)
            .select(
                ConnectionTable.id,
                UserTable.id,
                UserTable.username,
                UserTable.displayName,
                ConnectionTable.createdAt,
                UserTable.enabled,
            )
            .where {
                (ConnectionTable.senderUserId eq userId.value)
                    .andIfNotNull { deviceModelId?.let { DeviceTable.deviceModelId eq deviceModelId.value } }
                    .andIfNotNull { deviceOrientation?.let { DeviceTable.orientation eq deviceOrientation } }
            }
            .groupBy(
                ConnectionTable.id,
                UserTable.id,
                UserTable.username,
                UserTable.displayName,
                ConnectionTable.createdAt,
                UserTable.enabled,
            )
            .map {
                UserConnection(
                    connectionId = ConnectionId(it[ConnectionTable.id].value),
                    userId = UserId(it[UserTable.id].value),
                    username = it[UserTable.username],
                    displayName = it[UserTable.displayName],
                    createdAt = it[ConnectionTable.createdAt],
                    userEnabled = it[UserTable.enabled],
                )
            }

    fun checkUserHasActiveRecipient(fromUserId: UserId, toUserId: UserId): Boolean =
        ConnectionTable
            .innerJoin(UserTable) {
                ConnectionTable.recipientUserId eq UserTable.id
            }
            .select(ConnectionTable.id)
            .where {
                (ConnectionTable.senderUserId eq fromUserId.value) and
                        (ConnectionTable.recipientUserId eq toUserId.value) and
                        (UserTable.enabled eq true)
            }
            .any()

}