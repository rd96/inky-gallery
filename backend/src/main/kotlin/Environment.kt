package uk.derbyshire

import org.http4k.config.Environment as Environment4k
import org.http4k.config.EnvironmentKey
import org.http4k.config.Port
import org.http4k.lens.port

private val port = EnvironmentKey.port().required("port")

data class Environment(
    val serverConfig: ServerConfig,
) {
    companion object {
        private val defaultConfig = Environment4k.defaults(
            port of Port(8080),
        )

        fun fromEnv(): Environment {
            val env = Environment4k.ENV overrides defaultConfig

            return Environment(
                serverConfig = ServerConfig(
                    port = port(env).value,
                ),
            )
        }
    }
}

data class ServerConfig(
    val port: Int,
)
