package uk.derbyshire.api.admin

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.api.filters.CurrentUser

fun deleteUserActivationLink() = { request: Request ->
    val user = CurrentUser(request)

    Response(Status.NOT_IMPLEMENTED)
}