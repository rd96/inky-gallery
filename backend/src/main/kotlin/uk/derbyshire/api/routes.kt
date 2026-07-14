package uk.derbyshire.api

import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.auth.authRoutes
import uk.derbyshire.api.filters.AuthFilters
import uk.derbyshire.services.AuthService

fun apiRoutes(
    authFilters: AuthFilters,
    authService: AuthService,
    serverConfig: ServerConfig,
) = routes(
    "/auth" bind authRoutes(authFilters, authService, serverConfig)
)