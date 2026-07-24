package uk.derbyshire.api.devices

import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.services.DeviceService

fun deviceRoutes(deviceService: DeviceService) = routes(
    "/models" bind Method.GET to getDeviceModels(deviceService)
)