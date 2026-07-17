package uk.derbyshire.api.auth

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.domain.users.Role

fun getCurrentUser() = { request: Request ->
    val user = CurrentUser(request)

    Response(Status.OK).with(CurrentUserResponseDTO.lens.of(CurrentUserResponseDTO(user.username, user.role, user.displayName)))
}

private data class CurrentUserResponseDTO(
    val username: String,
    val role: Role,
    val displayName: String
) {
    companion object {
        val lens = Json.autoBody<CurrentUserResponseDTO>().toLens()
    }
}