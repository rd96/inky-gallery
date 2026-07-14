package uk.derbyshire.api.auth

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.config.Secret
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import org.http4k.core.cookie.cookie
import org.http4k.format.Jackson.auto
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.helpers.sessionCookie
import uk.derbyshire.services.AuthService
import uk.derbyshire.services.AuthService.Companion.SESSION_EXPIRES_AFTER

fun postLogin(authService: AuthService, serverConfig: ServerConfig): HttpHandler = { request: Request ->
    val loginRequest = LoginRequestDTO.lens(request)

    when (val loginResult = authService.login(loginRequest.username, loginRequest.password)) {
        is Success -> Response(Status.OK).cookie(sessionCookie(loginResult.value.sessionToken, serverConfig.secureSessionCookies))
        is Failure -> Response(Status.UNAUTHORIZED)
    }
}

private data class LoginRequestDTO(
    val username: String,
    val password: Secret,
) {
    companion object {
        val lens = Body.auto<LoginRequestDTO>().toLens()
    }
}

