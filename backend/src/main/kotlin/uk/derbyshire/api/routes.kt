package uk.derbyshire.api

import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.admin.adminRoutes
import uk.derbyshire.api.auth.authRoutes
import uk.derbyshire.api.filters.AuthChecker
import uk.derbyshire.services.AuthService
import uk.derbyshire.services.UserService

fun apiRoutes(
    authChecker: AuthChecker,
    userService: UserService,
    authService: AuthService,
    serverConfig: ServerConfig,
) = routes(
    "/auth" bind authRoutes(authChecker, authService, serverConfig),
    "/admin" bind authChecker.requireAdmin().then(adminRoutes(userService, authService))
)