package uk.derbyshire.api.helpers

import org.http4k.core.Request
import org.http4k.lens.PathLens
import org.http4k.lens.Path as Path4k
import org.http4k.lens.string
import uk.derbyshire.domain.connections.ConnectionId
import uk.derbyshire.domain.devices.DeviceId
import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.drawings.DrawingId
import uk.derbyshire.domain.users.UserId
import kotlin.uuid.Uuid

object PathParams {
    val userId = PathParam.uuid("userId", ::UserId)
    val connectionId = PathParam.uuid("connectionId", ::ConnectionId)
    val deviceId = PathParam.uuid("deviceId", ::DeviceId)
    val drawingId = PathParam.uuid("drawingId", ::DrawingId)
    val canvasId = PathParam.uuid("canvasId", ::CanvasId)
}

class PathParam<T> private constructor (
    val name: String,
    val lens: PathLens<T>,
) {
    val route = "{$name}"

    operator fun invoke(request: Request): T =
        lens(request)

    override fun toString(): String = route

    companion object {
        fun <T> uuid(
            name: String,
            constructor: (Uuid) -> T,
        ): PathParam<T> =
            PathParam(
                name = name,
                lens = Path4k
                    .string()
                    .map { constructor(Uuid.parse(it)) }
                    .of(name),
            )
    }
}



