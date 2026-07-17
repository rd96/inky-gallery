package uk.derbyshire.api.admin

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.Path

fun patchUser() = { request: Request ->
    val user = CurrentUser(request)
    val userId = Path.userId(request)

    Response(Status.NOT_IMPLEMENTED)
}