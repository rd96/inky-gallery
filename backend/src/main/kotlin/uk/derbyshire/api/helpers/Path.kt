package uk.derbyshire.api.helpers

import org.http4k.core.Request
import org.http4k.lens.Path as Path4k
import org.http4k.lens.string
import kotlin.uuid.Uuid

object Path {
    private const val USER_ID_PARAM_NAME = "userId"
    const val USER_ID = "{$USER_ID_PARAM_NAME}"
    fun userId(request: Request) = Path4k.string().map(Uuid::parse).of(USER_ID_PARAM_NAME)(request)
}
