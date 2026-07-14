package uk.derbyshire.api.auth

import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.filters.AuthFilters
import uk.derbyshire.services.AuthService

fun authRoutes(auth: AuthFilters, authService: AuthService, serverConfig: ServerConfig) = routes(
    "/me" bind Method.GET to auth.requireUser().then(getCurrentUser()),
    "/login" bind Method.POST to postLogin(authService, serverConfig),
    "/logout" bind Method.POST to auth.requireUser().then(postLogout(authService, serverConfig)),
)