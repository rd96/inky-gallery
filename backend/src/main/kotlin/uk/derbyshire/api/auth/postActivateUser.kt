package uk.derbyshire.api.auth

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.config.Secret
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.cookie
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.sessionCookie
import uk.derbyshire.domain.auth.ActivationFailure
import uk.derbyshire.services.AuthService

fun postActivateUser(authService: AuthService, serverConfig: ServerConfig) = { request: Request ->
    val activationRequest = ActivateUserRequestDTO.lens(request)

    when (val activationResult = authService.activateUser(activationRequest.activationToken, activationRequest.password)) {
        is Success -> Response(Status.NO_CONTENT).cookie(sessionCookie(activationResult.value.sessionToken, serverConfig.secureSessionCookies))
        is Failure -> {
            when (activationResult.reason) {
                ActivationFailure.PASSWORD_INVALID -> activationResult.reason.description.toErrorResponseDTO()
                else -> Response(Status.NOT_FOUND)
            }
        }
    }
}

private data class ActivateUserRequestDTO(
    val activationToken: Secret,
    val password: Secret,
) {
    companion object {
        val lens = Json.autoBody<ActivateUserRequestDTO>().toLens()
    }
}
