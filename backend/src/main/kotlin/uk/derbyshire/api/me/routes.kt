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
    "" bind Method.PATCH to patchOwnUser(services.userService),
    "" bind Method.DELETE to deleteOwnUser(services.authService, serverConfig),

    "/devices" bind Method.GET to getMyDevices(services.deviceService),
    "/devices" bind Method.POST to postRegisterDevice(services.deviceService),
    "/devices/$deviceId" bind Method.PATCH to patchOwnDevice(services.deviceService),
//    "/devices/{deviceId}/api-key" bind Method.PUT to putGenerateDeviceApiKey(deviceService),

    "/connections" bind Method.GET to getConnections(services.connectionService),
    "/recipients" bind Method.QUERY to queryRecipients(services.connectionService),

    "/canvases" bind Method.QUERY to queryMyCanvases(services.canvasService),
    "/canvases" bind Method.POST to postCreateCanvas(services.canvasService),
    "/canvases/$canvasId" bind Method.GET to getMyCanvas(services.canvasService),
    "/canvases/$canvasId/complete" bind Method.POST to postCompleteCanvas(services.canvasService),

    "/canvases/$canvasId/drawings" bind Method.POST to postCreateDrawing(services.drawingService),
    "/canvases/$canvasId/drawings" bind Method.PATCH to patchCanvasDrawings(services.canvasService),
    "/canvases/$canvasId/drawings/$drawingId" bind Method.GET to getDrawing(services.drawingService),
)