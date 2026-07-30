package uk.derbyshire.api.me

import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.helpers.PathParams.canvasId
import uk.derbyshire.api.helpers.PathParams.deviceId
import uk.derbyshire.api.helpers.PathParams.drawingId
import uk.derbyshire.services.AuthService
import uk.derbyshire.services.ConnectionService
import uk.derbyshire.services.DeviceService
import uk.derbyshire.services.DrawingService
import uk.derbyshire.services.UserService

fun userRoutes(connectionService: ConnectionService, deviceService: DeviceService, userService: UserService, authService: AuthService, drawingService: DrawingService, serverConfig: ServerConfig) = routes(
    "/connections" bind Method.GET to getConnections(connectionService),
    "/devices" bind Method.GET to getMyDevices(deviceService),
    "/devices" bind Method.POST to postRegisterDevice(deviceService),
    "/devices/$deviceId" bind Method.PATCH to patchOwnDevice(deviceService),
//    "/devices/{deviceId}/api-key" bind Method.PUT to putGenerateDeviceApiKey(deviceService),

    "" bind Method.PATCH to patchOwnUser(userService),
    "" bind Method.DELETE to deleteOwnUser(authService, serverConfig),

//    "/connections/recipients/$USER_ID/devices" bind Method.GET to getRecipientDevices(authService, serverConfig),

//    "/canvases" bind Method.QUERY to queryCanvases(),
//    "/canvases" bind Method.POST to postCreateCanvas(),

    "/canvases/$canvasId/drawings" bind Method.POST to postCreateDrawing(drawingService),
    "/canvases/$canvasId/drawings/$drawingId" bind Method.GET to getDrawing(drawingService),
//    "/drawings" bind Method.GET to getMyDrawings(drawingService),
//    "/drawings" bind Method.POST to postCreateDrawing(drawingService),
//
//    "/drawings/$DRAWING_ID" bind Method.GET to getDrawing(drawingService),
)