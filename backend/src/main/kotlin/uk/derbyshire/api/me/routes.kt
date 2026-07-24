package uk.derbyshire.api.me

import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.helpers.Path.DEVICE_ID
import uk.derbyshire.services.AuthService
import uk.derbyshire.services.ConnectionService
import uk.derbyshire.services.DeviceService
import uk.derbyshire.services.UserService

fun userRoutes(connectionService: ConnectionService, deviceService: DeviceService, userService: UserService, authService: AuthService, serverConfig: ServerConfig) = routes(
    "/connections" bind Method.GET to getConnections(connectionService),
    "/devices" bind Method.GET to getMyDevices(deviceService),
    "/devices" bind Method.POST to postRegisterDevice(deviceService),
    "/devices/$DEVICE_ID" bind Method.PATCH to patchOwnDevice(deviceService),
//    "/devices/{deviceId}/api-key" bind Method.PUT to putGenerateDeviceApiKey(deviceService),
//
    "" bind Method.PATCH to patchOwnUser(userService),
    "" bind Method.DELETE to deleteOwnUser(authService, serverConfig),
)