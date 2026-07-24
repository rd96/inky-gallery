package uk.derbyshire.api.me

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.domain.devices.Orientation
import kotlin.uuid.Uuid

fun postRegisterDevice() = { request: Request ->
    Response(Status.NOT_IMPLEMENTED)
}

data class PostRegisterDeviceRequestDTO(
    val deviceName: String,
    val deviceTypeId: Uuid,
    val orientation: Orientation,
)



