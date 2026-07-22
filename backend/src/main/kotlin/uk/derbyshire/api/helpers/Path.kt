package uk.derbyshire.api.helpers

import org.http4k.core.Request
import org.http4k.lens.Path as Path4k
import org.http4k.lens.string
import uk.derbyshire.domain.users.UserId
import kotlin.uuid.Uuid

object Path {
    private const val USER_ID_PARAM_NAME = "userId"
    const val USER_ID = "{$USER_ID_PARAM_NAME}"
    fun userId(request: Request) = Path4k.string().map { UserId(Uuid.parse(it)) }.of(USER_ID_PARAM_NAME)(request)

    private const val CONNECTION_ID_PARAM_NAME = "connectionId"
    const val CONNECTION_ID = "{$CONNECTION_ID_PARAM_NAME}"
    fun connectionId(request: Request) = Path4k.string().map(Uuid::parse).of(CONNECTION_ID_PARAM_NAME)(request)
}
