package uk.derbyshire.api.me

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.services.UserService

fun patchOwnUser(userService: UserService) = { request: Request ->
    val currentUser = CurrentUser(request)

    val patchUserRequest = PatchOwnUserRequestDTO.lens(request)

    val result = userService.updateUserDisplayName(
        userId = currentUser.userId,
        displayName = patchUserRequest.displayName,
    )

    when (result) {
        is Success -> Response(Status.OK)
        is Failure -> result.reason.description.toErrorResponseDTO()
    }
}

data class PatchOwnUserRequestDTO(
    val displayName: String,
) {
    companion object {
        val lens = Json.autoBody<PatchOwnUserRequestDTO>().toLens()
    }
}