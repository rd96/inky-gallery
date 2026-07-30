package uk.derbyshire.database.repositories

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.http4k.config.Secret
import org.jetbrains.exposed.v1.core.LikePattern
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.insertIgnoreAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.postgresql.util.PSQLState
import uk.derbyshire.domain.users.Role
import uk.derbyshire.database.schema.UserTable
import uk.derbyshire.domain.auth.ActivationFailure
import uk.derbyshire.domain.users.ActivationStatus
import uk.derbyshire.domain.users.UpdateUserFailure
import uk.derbyshire.domain.users.UserId
import uk.derbyshire.domain.users.UserLoginDetail
import uk.derbyshire.domain.users.UserSearchResult
import uk.derbyshire.domain.users.UserSummary

class UserRepository {
    fun createUser(username: String, passwordHash: String?, displayName: String, role: Role, activationStatus: ActivationStatus): UserId? =
        UserTable.insertIgnoreAndGetId {
            it[this.username] = username
            it[this.passwordHash] = passwordHash
            it[this.displayName] = displayName
            it[this.role] = role
            it[this.activationStatus] = activationStatus
        }?.let { UserId(it.value) }

    fun hasAdminUser(): Boolean =
        UserTable
            .select(UserTable.id)
            .where { UserTable.role eq Role.ADMIN }
            .limit(1)
            .any()

    fun countEnabledAdmins(): Long =
        UserTable.select(UserTable.id)
            .where { UserTable.enabled and (UserTable.role eq Role.ADMIN) }
            .count()

    fun findUser(userId: UserId): UserSummary? =
        UserTable.select(
            UserTable.id,
            UserTable.username,
            UserTable.displayName,
            UserTable.role,
            UserTable.activationStatus,
            UserTable.enabled,
            UserTable.createdAt,
        )
            .where { UserTable.id eq userId.value }
            .singleOrNull()
            ?.let(::toUserSummary)

    fun findUserLoginByUsername(username: String): UserLoginDetail? =
        UserTable.select(
            UserTable.id,
            UserTable.username,
            UserTable.passwordHash,
            UserTable.role,
            UserTable.activationStatus,
            UserTable.enabled
        )
            .where { UserTable.username eq username }
            .singleOrNull()
            ?.let {
                UserLoginDetail(
                    UserId(it[UserTable.id].value),
                    it[UserTable.username],
                    it[UserTable.passwordHash]?.let(::Secret),
                    it[UserTable.role],
                    it[UserTable.activationStatus],
                    it[UserTable.enabled],
                )
            }

    fun setPendingUserPasswordAndStatusActivated(userId: UserId, passwordHash: String): Result4k<Unit, ActivationFailure> {
        val result = UserTable.update({ (UserTable.id eq userId.value) and (UserTable.activationStatus eq ActivationStatus.PENDING) }) {
            it[UserTable.passwordHash] = passwordHash
            it[UserTable.activationStatus] = ActivationStatus.ACTIVATED
        }

        return if (result == 0) Failure(ActivationFailure.PENDING_USER_NOT_FOUND)
        else Success(Unit)
    }


    fun searchUsers(nameSearch: String?, role: Role?, activationStatus: ActivationStatus?, enabled: Boolean?, limit: Int, page: Int): UserSearchResult {
        val query = UserTable.select(
            UserTable.id,
            UserTable.username,
            UserTable.displayName,
            UserTable.role,
            UserTable.activationStatus,
            UserTable.enabled,
            UserTable.createdAt,
        )

        nameSearch?.takeIf(String::isNotBlank)
            ?.lowercase()
            ?.let {
                val pattern = searchLikePattern(it)

                query.andWhere {
                    (UserTable.username.lowerCase() like pattern) or
                    (UserTable.displayName.lowerCase() like pattern)}
            }

        role?.let {
            query.andWhere { UserTable.role eq role }
        }

        activationStatus?.let {
            query.andWhere { UserTable.activationStatus eq activationStatus }
        }

        enabled?.let {
            query.andWhere { UserTable.enabled eq enabled }
        }

        val total = query.count()

        val users = query
            .orderBy(
                UserTable.displayName.lowerCase() to SortOrder.ASC,
                UserTable.id to SortOrder.ASC,
            )
            .limit(limit)
            .offset(((page - 1L) * limit))
            .map(::toUserSummary)

        return UserSearchResult(
            users,
            total,
        )
    }

    fun updateUser(userId: UserId, username: String?, displayName: String?, enabled: Boolean?, role: Role?): Result4k<Unit, UpdateUserFailure> =
        try {
            val success = UserTable.update({ UserTable.id eq userId.value }) { table ->
                username?.let { table[this.username] = it }
                displayName?.let { table[this.displayName] = it }
                role?.let { table[this.role] = it }
                enabled?.let { table[this.enabled] = it }
            } == 1

            if (success) Success(Unit) else Failure(UpdateUserFailure.USER_NOT_FOUND)
        } catch (e: ExposedSQLException) {
            if (e.sqlState == PSQLState.UNIQUE_VIOLATION.state) Failure(UpdateUserFailure.USERNAME_ALREADY_IN_USE)
            else throw e
        }


    fun userExists(userId: UserId): Boolean =
        UserTable.select(UserTable.id)
            .where { UserTable.id eq userId.value }
            .any()

    fun disableUser(userId: UserId) {
        UserTable.update({ UserTable.id eq userId.value }) {
            it[enabled] = false
        }
    }

    companion object {
        private fun searchLikePattern(search: String, escapeChar: Char = '!'): LikePattern {
            val escapedSearch = search
                .replace("$escapeChar", "$escapeChar$escapeChar")
                .replace("%", "$escapeChar%")
                .replace("_", "${escapeChar}_")

            return LikePattern(
                "%$escapedSearch%",
                escapeChar,
            )
        }

        private fun toUserSummary(row: ResultRow) = UserSummary(
            UserId(row[UserTable.id].value),
            row[UserTable.username],
            row[UserTable.displayName],
            row[UserTable.role],
            row[UserTable.activationStatus],
            row[UserTable.enabled],
            row[UserTable.createdAt],
        )
    }

}