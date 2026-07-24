package uk.derbyshire.database.repositories

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import uk.derbyshire.database.schema.DeviceApiKeyTable
import uk.derbyshire.database.schema.DeviceTable
import uk.derbyshire.database.schema.UserTable
import uk.derbyshire.domain.auth.ApiKeyUser
import uk.derbyshire.domain.users.UserId
import kotlin.time.Instant
import kotlin.uuid.Uuid

class DeviceApiKeyRepository {
    fun createDeviceApiKey(deviceId: Uuid, keyHash: String, keyPrefix: String, createdAt: Instant) {
        DeviceApiKeyTable.insert {
            it[this.deviceId] = deviceId
            it[this.keyHash] = keyHash
            it[this.keyPrefix] = keyPrefix
            it[this.createdAt] = createdAt
        }
    }

    fun findUserByApiKeyHash(apiKeyHash: String): ApiKeyUser? =
        DeviceApiKeyTable
            .innerJoin(DeviceTable)
            .innerJoin(UserTable)
            .select(
                UserTable.id,
                UserTable.enabled,
                DeviceTable.id,
                DeviceTable.enabled,
                DeviceApiKeyTable.revokedAt,
            )
            .where { DeviceApiKeyTable.keyHash eq apiKeyHash }
            .singleOrNull()
            ?.let {
                ApiKeyUser(
                    userId = UserId(it[UserTable.id].value),
                    userEnabled = it[UserTable.enabled],
                    deviceId = it[DeviceTable.id].value,
                    deviceEnabled = it[DeviceTable.enabled],
                    revokedAt = it[DeviceApiKeyTable.revokedAt],
                )
            }

    fun revokeApiKeysForDevice(deviceId: Uuid, revokedAt: Instant) {
        DeviceApiKeyTable.update({
            (DeviceApiKeyTable.deviceId eq deviceId) and (DeviceApiKeyTable.revokedAt.isNull())
        }) {
            it[this.revokedAt] = revokedAt
        }
    }
}