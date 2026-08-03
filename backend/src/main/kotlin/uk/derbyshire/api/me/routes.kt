package uk.derbyshire.api.me

import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.ServerConfig
import uk.derbyshire.Services
import uk.derbyshire.api.helpers.PathParams.canvasId
import uk.derbyshire.api.helpers.PathParams.deviceId
import uk.derbyshire.api.helpers.PathParams.drawingId

fun userRoutes(services: Services, serverConfig: ServerConfig) = routes(
    "/connections" bind Method.GET to getConnections(services.connectionService),
    "/devices" bind Method.GET to getMyDevices(services.deviceService),
    "/devices" bind Method.POST to postRegisterDevice(services.deviceService),
    "/devices/$deviceId" bind Method.PATCH to patchOwnDevice(services.deviceService),
//    "/devices/{deviceId}/api-key" bind Method.PUT to putGenerateDeviceApiKey(deviceService),

    "" bind Method.PATCH to patchOwnUser(services.userService),
    "" bind Method.DELETE to deleteOwnUser(services.authService, serverConfig),

//    "/connections/recipients/$USER_ID/devices" bind Method.GET to getRecipientDevices(authService, serverConfig),

    "/canvases" bind Method.QUERY to queryMyCanvases(services.canvasService),
    "/canvases" bind Method.POST to postCreateCanvas(services.canvasService),
    "/canvases/$canvasId" bind Method.GET to getMyCanvas(services.canvasService),

    "/canvases/$canvasId/drawings" bind Method.POST to postCreateDrawing(services.drawingService),
    "/canvases/$canvasId/drawings/$drawingId" bind Method.GET to getDrawing(services.drawingService),


)