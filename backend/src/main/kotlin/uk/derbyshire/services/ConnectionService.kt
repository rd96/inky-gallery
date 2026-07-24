package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.asSuccess
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.ConnectionRepository
import uk.derbyshire.domain.connections.CreateConnectionFailure
import uk.derbyshire.domain.connections.GetConnectionsFailure
import uk.derbyshire.domain.connections.UserConnection
import uk.derbyshire.domain.connections.UserConnections
import uk.derbyshire.domain.users.UserId
import kotlin.uuid.Uuid

class ConnectionService(
    private val connectionRepository: ConnectionRepository,
    private val userService: UserService,
    private val context: DatabaseContext,
) {
    fun createUserConnection(senderUserId: UserId, recipientUserId: UserId, createdBy: UserId): Result4k<Uuid, CreateConnectionFailure> {
        if (!userService.userExists(senderUserId)) return Failure(CreateConnectionFailure.SENDER_NOT_FOUND)
        if (!userService.userExists(recipientUserId)) return Failure(CreateConnectionFailure.RECIPIENT_NOT_FOUND)

        return context.transaction {
            connectionRepository.insertConnection(senderUserId, recipientUserId, createdBy)
                .asResultOr { CreateConnectionFailure.RECIPIENT_NOT_FOUND }
        }
    }

    fun deleteUserConnection(connectionId: Uuid) {
        context.transaction {
            connectionRepository.deleteConnection(connectionId)
        }
    }

    fun getAllConnectionsForUser(userId: UserId): Result4k<UserConnections, GetConnectionsFailure> =
        if (!userService.userExists(userId)) Failure(GetConnectionsFailure.USER_NOT_FOUND)
        else UserConnections(
            senders = findSendersFor(userId),
            recipients = findRecipientsFor(userId),
        ).asSuccess()

    fun getActiveConnectionsForUser(userId: UserId): Result4k<UserConnections, GetConnectionsFailure> =
        if (!userService.userExists(userId)) Failure(GetConnectionsFailure.USER_NOT_FOUND)
        else UserConnections(
            senders = findSendersFor(userId, onlyEnabled = true),
            recipients = findRecipientsFor(userId),
        ).asSuccess()

    private fun findRecipientsFor(userId: UserId, onlyEnabled: Boolean = false): List<UserConnection> =
        context.transaction {
            connectionRepository.getRecipientsFor(userId, onlyEnabled)
        }

    private fun findSendersFor(userId: UserId, onlyEnabled: Boolean = false): List<UserConnection> =
        context.transaction {
            connectionRepository.getSendersFor(userId, onlyEnabled)
        }

}