package uk.derbyshire.api.admin

import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.api.helpers.Path.CONNECTION_ID
import uk.derbyshire.api.helpers.Path.USER_ID
import uk.derbyshire.services.AuthService
import uk.derbyshire.services.ConnectionsService
import uk.derbyshire.services.UserService

fun adminRoutes(userService: UserService, authService: AuthService, connectionsService: ConnectionsService) = routes(
    "/users" bind Method.QUERY to queryUsers(userService),
    "/users" bind Method.POST to postCreateUser(authService),
    "/users/$USER_ID" bind Method.PUT to patchUser(userService),

    "/users/$USER_ID/activation-tokens" bind Method.POST to postCreateUserActivationToken(authService),
    "/users/$USER_ID/activation-tokens" bind Method.DELETE to deleteUserActivationTokens(authService),

    "/users/$USER_ID/connections" bind Method.GET to getUserConnections(connectionsService),
    "/connections" bind Method.POST to postCreateUserConnection(connectionsService),
    "/connections/$CONNECTION_ID" bind Method.DELETE to deleteUserConnection(connectionsService)
)