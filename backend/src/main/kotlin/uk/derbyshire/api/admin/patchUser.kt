package uk.derbyshire.api.admin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.Path
import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.UpdateUserFailure
import uk.derbyshire.services.UserService

fun patchUser(userService: UserService) = { request: Request ->
    val userId = Path.userId(request)
    val patchUserRequest = PatchUserRequestDTO.lens(request)

    val result = userService.updateUser(
        userId,
        patchUserRequest.username,
        patchUserRequest.displayName,
        patchUserRequest.enabled,
        patchUserRequest.role,
    )

    when (result) {
        is Success -> Response(Status.NO_CONTENT)
        is Failure -> when (result.reason) {
            UpdateUserFailure.USER_NOT_FOUND -> result.reason.description.toErrorResponseDTO(NOT_FOUND)
            else -> result.reason.description.toErrorResponseDTO(BAD_REQUEST)
        }
    }
}

data class PatchUserRequestDTO(
    val username: String? = null,
    val displayName: String? = null,
    val enabled: Boolean? = null,
    val role: Role? = null,
) {
    companion object {
        val lens = Json.autoBody<PatchUserRequestDTO>().toLens()
    }
}