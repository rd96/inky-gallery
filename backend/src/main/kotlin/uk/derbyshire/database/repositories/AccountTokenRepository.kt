package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import uk.derbyshire.database.schema.AccountTokenTable
import uk.derbyshire.domain.auth.AccountTokenType
import uk.derbyshire.domain.auth.AccountToken
import uk.derbyshire.domain.users.UserId
import kotlin.time.Instant

class AccountTokenRepository {
    fun createAccountToken(userId: UserId, tokenType: AccountTokenType, tokenHash: String, expiresAt: Instant, createdBy: UserId, createdAt: Instant) {
        AccountTokenTable.insert {
            it[this.userId] = userId.value
            it[this.tokenType] = tokenType
            it[this.tokenHash] = tokenHash
            it[this.createdAt] = createdAt
            it[this.createdBy] = createdBy.value
            it[this.expiresAt] = expiresAt
        }
    }

    fun revokeTokensForUser(userId: UserId, tokenType: AccountTokenType, revokedAt: Instant) {
        AccountTokenTable.update({
            (AccountTokenTable.userId eq userId.value) and (AccountTokenTable.tokenType eq tokenType) and (AccountTokenTable.revokedAt.isNull() and (AccountTokenTable.usedAt.isNull()))
        }) {
            it[this.revokedAt] = revokedAt
        }
    }

    fun getAccountTokenByHash(tokenHash: String, tokenType: AccountTokenType): AccountToken? =
        AccountTokenTable.select(
            AccountTokenTable.userId,
            AccountTokenTable.expiresAt,
            AccountTokenTable.usedAt,
            AccountTokenTable.revokedAt,
        ).where { (AccountTokenTable.tokenHash eq tokenHash) and (AccountTokenTable.tokenType eq tokenType) }
            .singleOrNull()
            ?.let {
                AccountToken(
                    UserId(it[AccountTokenTable.userId].value),
                    it[AccountTokenTable.expiresAt],
                    it[AccountTokenTable.usedAt],
                    it[AccountTokenTable.revokedAt],
                )
            }
}