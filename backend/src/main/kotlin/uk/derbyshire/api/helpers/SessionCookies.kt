package uk.derbyshire.api.helpers

import org.http4k.config.Secret
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import uk.derbyshire.services.AuthService.Companion.SESSION_EXPIRES_AFTER

const val SESSION_COOKIE_NAME = "session"

fun sessionCookie(token: Secret, secure: Boolean): Cookie =
    token.use {
        Cookie(
            name = "session",
            value = it,
            httpOnly = true,
            secure = secure,
            sameSite = SameSite.Lax,
            path = "/",
            maxAge = SESSION_EXPIRES_AFTER.inWholeSeconds,
        )
    }

fun expiredSessionCookie(secure: Boolean): Cookie =
    Cookie(
        name = SESSION_COOKIE_NAME,
        value = "",
        httpOnly = true,
        secure = secure,
        sameSite = SameSite.Lax,
        path = "/",
        maxAge = 0,
    )
