package uk.derbyshire.api.auth

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.UserId

fun getCurrentUser() = { request: Request ->
    val user = CurrentUser(request)
    
    val response = CurrentUserResponseDTO(
        userId = user.userId,
        username = user.username,
        role = user.role,
        displayName = user.displayName
    )

    Response(Status.OK).with(CurrentUserResponseDTO.lens.of(response))
}

private data class CurrentUserResponseDTO(
    val userId: UserId,
    val username: String,
    val role: Role,
    val displayName: String
) {
    companion object {
        val lens = Json.autoBody<CurrentUserResponseDTO>().toLens()
    }
}