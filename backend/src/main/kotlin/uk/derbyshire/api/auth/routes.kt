package uk.derbyshire.api.auth

import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.filters.AuthChecker
import uk.derbyshire.services.AuthService

fun authRoutes(authChecker: AuthChecker, authService: AuthService, serverConfig: ServerConfig) = routes(
    "/me" bind Method.GET to authChecker.requireUser().then(getCurrentUser()),
    "/login" bind Method.POST to postLogin(authService, serverConfig),
    "/activate" bind Method.QUERY to queryActivationToken(authService),
    "/activate" bind Method.POST to postActivateUser(authService, serverConfig),
    "/logout" bind Method.POST to authChecker.requireUser().then(postLogout(authService, serverConfig))
)