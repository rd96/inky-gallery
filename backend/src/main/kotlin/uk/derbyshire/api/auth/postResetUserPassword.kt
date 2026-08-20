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
import uk.derbyshire.domain.auth.PasswordResetFailure
import uk.derbyshire.services.AuthService

fun postResetUserPassword(authService: AuthService, serverConfig: ServerConfig) = { request: Request ->
    val resetRequest = PostActivateUserRequestDTO.lens(request)

    when (val resetResult = authService.resetUserPassword(resetRequest.passwordResetToken, resetRequest.password)) {
        is Success -> Response(Status.NO_CONTENT).cookie(sessionCookie(resetResult.value.sessionToken, serverConfig.secureSessionCookies))
        is Failure -> {
            when (resetResult.reason) {
                PasswordResetFailure.PASSWORD_INVALID -> resetResult.reason.description.toErrorResponseDTO()
                else -> Response(Status.NOT_FOUND)
            }
        }
    }
}

private data class PostActivateUserRequestDTO(
    val passwordResetToken: Secret,
    val password: Secret,
) {
    companion object {
        val lens = Json.autoBody<PostActivateUserRequestDTO>().toLens()
    }
}