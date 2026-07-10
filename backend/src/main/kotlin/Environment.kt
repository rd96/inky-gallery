package uk.derbyshire

import org.http4k.config.Environment as Environment4k
import org.http4k.config.EnvironmentKey
import org.http4k.config.Port
import org.http4k.config.Secret
import org.http4k.core.Uri
import org.http4k.lens.port
import org.http4k.lens.secret
import org.http4k.lens.uri
import uk.derbyshire.ServerConfig.Companion.port

data class Environment(
    val serverConfig: ServerConfig,
    val databaseConfig: DatabaseConfig,
    val adminUserConfig: AdminUserConfig? = null,
) {
    companion object {
        private val defaultConfig = Environment4k.defaults(
            port of Port(8080),
        )

        fun fromEnv(): Environment {
            val env = Environment4k.ENV overrides defaultConfig

            return Environment(
                serverConfig = ServerConfig.from(env),
                databaseConfig = DatabaseConfig.from(env),
                adminUserConfig = AdminUserConfig.from(env),
            )
        }
    }
}

data class ServerConfig (
    val port: Int,
) {
    companion object {
        val port = EnvironmentKey.port().required("PORT")

        fun from(env: Environment4k) = ServerConfig(
            port = port(env).value,
        )
    }
}

data class DatabaseConfig(
    val url: Uri,
    val username: Secret,
    val password: Secret,
) {
    companion object {
        private val url = EnvironmentKey.uri().required("DATABASE_URL")
        private val username = EnvironmentKey.secret().required("DATABASE_USERNAME")
        private val password = EnvironmentKey.secret().required("DATABASE_PASSWORD")

        fun from(env: Environment4k) = DatabaseConfig(
            url = url(env),
            username = username(env),
            password = password(env),
        )
    }
}

data class AdminUserConfig(
    val username: Secret,
    val password: Secret,
) {
    companion object {
        private val username = EnvironmentKey.secret().optional("INITIAL_ADMIN_USERNAME")
        private val password = EnvironmentKey.secret().optional("INITIAL_ADMIN_USERNAME")

        fun from(env: Environment4k): AdminUserConfig? {
            val username = username(env) ?: return null
            val password = password(env) ?: return null

            return AdminUserConfig(username, password)
        }
    }
}
