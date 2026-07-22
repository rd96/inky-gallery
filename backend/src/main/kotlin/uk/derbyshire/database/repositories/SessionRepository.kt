package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import uk.derbyshire.database.schema.SessionTable
import uk.derbyshire.database.schema.UserTable
import uk.derbyshire.domain.auth.SessionUser
import uk.derbyshire.domain.users.UserId
import kotlin.time.Instant
import kotlin.uuid.Uuid

class SessionRepository {
    fun findUserBySessionTokenHash(tokenHash: String): SessionUser? =
        SessionTable.innerJoin(UserTable)
            .select(
                SessionTable.id,
                SessionTable.expiresAt,
                UserTable.id,
                UserTable.username,
                UserTable.role,
                UserTable.displayName,
                UserTable.activationStatus,
                UserTable.enabled,
            )
            .where(SessionTable.tokenHash eq tokenHash)
            .singleOrNull()?.let {
                SessionUser(
                    it[SessionTable.id].value,
                    it[SessionTable.expiresAt],
                    UserId(it[UserTable.id].value),
                    it[UserTable.username],
                    it[UserTable.role],
                    it[UserTable.displayName],
                    it[UserTable.activationStatus],
                    it[UserTable.enabled],
                )
            }

    fun deleteSession(sessionId: Uuid) {
        SessionTable.deleteWhere { SessionTable.id eq sessionId }
    }

    fun deleteByTokenHash(tokenHash: String) {
        SessionTable.deleteWhere { SessionTable.tokenHash eq tokenHash }
    }

    fun deleteExpiredSessions(now: Instant) {
        SessionTable.deleteWhere { SessionTable.expiresAt less now }
    }

    fun updateLastSeen(sessionId: Uuid, now: Instant) {
        SessionTable.update({ SessionTable.id eq sessionId }) {
            it[lastSeenAt] = now
        }
    }

    fun createSession(userId: UserId, tokenHash: String, expiresAt: Instant) {
        SessionTable.insert {
            it[SessionTable.userId] = userId.value
            it[SessionTable.tokenHash] = tokenHash
            it[SessionTable.expiresAt] = expiresAt
        }
    }
}