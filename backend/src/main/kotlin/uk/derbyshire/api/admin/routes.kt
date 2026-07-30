package uk.derbyshire.api.admin

import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.api.helpers.PathParams.connectionId
import uk.derbyshire.api.helpers.PathParams.userId
import uk.derbyshire.services.AuthService
import uk.derbyshire.services.ConnectionService
import uk.derbyshire.services.DeviceService
import uk.derbyshire.services.UserService

fun adminRoutes(userService: UserService, authService: AuthService, connectionService: ConnectionService, deviceService: DeviceService) = routes(
    "/users" bind Method.QUERY to queryUsers(userService),
    "/users" bind Method.POST to postCreateUser(userService),
    "/users/$userId" bind Method.GET to getUser(userService),
    "/users/$userId" bind Method.PATCH to patchUser(userService),

    "/users/$userId/activation-tokens" bind Method.PUT to putGenerateUserActivationToken(authService),
    "/users/$userId/activation-tokens" bind Method.DELETE to deleteUserActivationTokens(authService),

    "/users/$userId/connections" bind Method.GET to getUserConnections(connectionService),
    "/connections" bind Method.POST to postCreateUserConnection(connectionService),
    "/connections/$connectionId" bind Method.DELETE to deleteUserConnection(connectionService),

    "/device-models" bind Method.POST to postCreateDeviceModel(deviceService),
)