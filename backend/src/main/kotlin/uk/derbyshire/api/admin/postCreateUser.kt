package uk.derbyshire.api.admin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.domain.users.Role
import uk.derbyshire.services.AuthService
import kotlin.uuid.Uuid

fun postCreateUser(authService: AuthService) = request@{ request: Request ->
    val user = CurrentUser(request)
    val userRequest = PostCreateUserRequestDTO.lens(request)

    val pendingUserResult = authService.createPendingUser(
        username = userRequest.username,
        displayName = userRequest.displayName,
        role = user.role,
        createdBy = user.userId,
    )

    when (pendingUserResult) {
        is Success -> {}
        is Failure -> {}
    }

    Response(Status.NOT_IMPLEMENTED)
}

data class PostCreateUserRequestDTO(
    val username: String,
    val displayName: String,
    val role: Role,
) {
    companion object {
        val lens = Json.autoBody<PostCreateUserRequestDTO>().toLens()
    }
}

data class PostCreateUserResponseDTO(
    val userId: Uuid,
    val activationToken: String,
)
