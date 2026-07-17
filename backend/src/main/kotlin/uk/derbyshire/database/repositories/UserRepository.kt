package uk.derbyshire.database.repositories

import org.http4k.config.Secret
import org.jetbrains.exposed.v1.core.LikePattern
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import uk.derbyshire.domain.users.Role
import uk.derbyshire.database.schema.UserTable
import uk.derbyshire.domain.users.ActivationStatus
import uk.derbyshire.domain.users.UserLoginDetail
import uk.derbyshire.domain.users.UserSearchResult
import uk.derbyshire.domain.users.UserSummary
import kotlin.uuid.Uuid

class UserRepository {
    fun createUser(username: String, passwordHash: String?, displayName: String, role: Role, activationStatus: ActivationStatus): Uuid =
        UserTable.insertAndGetId {
            it[this.username] = username
            it[this.passwordHash] = passwordHash
            it[this.displayName] = displayName
            it[this.role] = role
            it[this.activationStatus] = activationStatus
        }.value

    fun hasAdminUser(): Boolean =
        UserTable
            .select(UserTable.id)
            .where { UserTable.role eq Role.ADMIN }
            .limit(1)
            .any()

    fun findUser(userId: Uuid): UserSummary? =
        UserTable.select(
            UserTable.id,
            UserTable.username,
            UserTable.displayName,
            UserTable.role,
            UserTable.activationStatus,
            UserTable.enabled,
            UserTable.createdAt,
        )
            .where { UserTable.id eq userId }
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
                    it[UserTable.id].value,
                    it[UserTable.username],
                    it[UserTable.passwordHash]?.let(::Secret),
                    it[UserTable.role],
                    it[UserTable.activationStatus],
                    it[UserTable.enabled],
                )
            }

    fun setUserPasswordAndStatus(userId: Uuid, passwordHash: String?, activationStatus: ActivationStatus) =
        UserTable.update({ UserTable.id eq userId }) {
            it[UserTable.passwordHash] = passwordHash
            it[UserTable.activationStatus] = activationStatus
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
            row[UserTable.id].value,
            row[UserTable.username],
            row[UserTable.displayName],
            row[UserTable.role],
            row[UserTable.activationStatus],
            row[UserTable.enabled],
            row[UserTable.createdAt],
        )
    }

}