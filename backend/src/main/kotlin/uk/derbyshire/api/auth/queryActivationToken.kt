package uk.derbyshire.api.auth

import org.http4k.config.Secret
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.services.AuthService
import kotlin.time.Instant

fun queryActivationToken(authService: AuthService, serverConfig: ServerConfig) = { request: Request ->
    val activationTokenRequest = QueryActivationTokenRequest.lens(request)

    Response(Status.NOT_IMPLEMENTED)
}

private data class QueryActivationTokenRequest(
    val activationToken: Secret,
) {
    companion object {
        val lens = Json.autoBody<QueryActivationTokenRequest>().toLens()
    }
}

private data class QueryActivationTokenResponse(
    val username: String,
    val displayName: String,
    val expiresAt: Instant,
)