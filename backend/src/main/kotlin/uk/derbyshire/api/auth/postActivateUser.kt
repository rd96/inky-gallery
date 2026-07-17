package uk.derbyshire.api.auth

import org.http4k.config.Secret
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.services.AuthService

fun postActivateUser(authService: AuthService, serverConfig: ServerConfig) = { request: Request ->
    val activateRequest = ActivateUserRequestDTO.lens(request)

    Response(Status.NOT_IMPLEMENTED)
}

private data class ActivateUserRequestDTO(
    val activationToken: Secret,
    val password: Secret,
) {
    companion object {
        val lens = Json.autoBody<ActivateUserRequestDTO>().toLens()
    }
}