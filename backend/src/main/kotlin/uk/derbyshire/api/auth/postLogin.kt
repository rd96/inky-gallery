package uk.derbyshire.api.auth

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.config.Secret
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.cookie
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.sessionCookie
import uk.derbyshire.services.AuthService

fun postLogin(authService: AuthService, serverConfig: ServerConfig) = { request: Request ->
    val loginRequest = LoginRequestDTO.lens(request)

    when (val loginResult = authService.login(loginRequest.username, loginRequest.password)) {
        is Success -> Response(Status.NO_CONTENT).cookie(sessionCookie(loginResult.value.sessionToken, serverConfig.secureSessionCookies))
        is Failure -> Response(Status.UNAUTHORIZED)
    }
}

private data class LoginRequestDTO(
    val username: String,
    val password: Secret,
) {
    companion object {
        val lens = Json.autoBody<LoginRequestDTO>().toLens()
    }
}

