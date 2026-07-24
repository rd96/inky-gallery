package uk.derbyshire.api

import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.admin.adminRoutes
import uk.derbyshire.api.auth.authRoutes
import uk.derbyshire.api.filters.AuthChecker
import uk.derbyshire.api.me.userRoutes
import uk.derbyshire.api.search.searchRoutes
import uk.derbyshire.services.AuthService
import uk.derbyshire.services.ConnectionService
import uk.derbyshire.services.DeviceService
import uk.derbyshire.services.UserService

fun apiRoutes(
    authChecker: AuthChecker,
    userService: UserService,
    authService: AuthService,
    connectionService: ConnectionService,
    deviceService: DeviceService,
    serverConfig: ServerConfig,
) = routes(
    "/auth" bind authRoutes(authChecker, authService, serverConfig),
    "/admin" bind authChecker.requireAdmin().then(adminRoutes(userService, authService, connectionService, deviceService)),
    "/me" bind authChecker.requireUser().then(userRoutes(connectionService, deviceService, userService)),
    "/search" bind authChecker.requireUser().then(searchRoutes(deviceService))
)