package uk.derbyshire.api.search

import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.services.DeviceService

fun searchRoutes(deviceService: DeviceService) = routes(
    "/device-models" bind Method.GET to getDeviceModels(deviceService)
)