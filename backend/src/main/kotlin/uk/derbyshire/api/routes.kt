package uk.derbyshire.api

import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.ServerConfig
import uk.derbyshire.Services
import uk.derbyshire.api.admin.adminRoutes
import uk.derbyshire.api.auth.authRoutes
import uk.derbyshire.api.filters.AuthChecker
import uk.derbyshire.api.me.userRoutes
import uk.derbyshire.api.search.searchRoutes

fun apiRoutes(
    authChecker: AuthChecker,
    services: Services,
    serverConfig: ServerConfig,
) = routes(
    "/auth" bind authRoutes(authChecker, services.authService, serverConfig),
    "/admin" bind authChecker.requireAdmin().then(adminRoutes(services.userService, services.authService, services.connectionService, services.deviceService)),
    "/me" bind authChecker.requireUser().then(userRoutes(services, serverConfig)),
    "/search" bind authChecker.requireUser().then(searchRoutes(services.deviceService))
)