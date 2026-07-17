package uk.derbyshire.api.auth

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.cookie
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.helpers.SESSION_COOKIE_NAME
import uk.derbyshire.api.helpers.expiredSessionCookie
import uk.derbyshire.services.AuthService

fun postLogout(authService: AuthService, serverConfig: ServerConfig) = { request: Request ->
    request.cookie(SESSION_COOKIE_NAME)?.let {
        authService.logout(it.value)
    }

    Response(Status.NO_CONTENT).cookie(expiredSessionCookie(serverConfig.secureSessionCookies))
}