package uk.derbyshire.api.admin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.admin.PostCreateUserResponseDTO.Companion.toDto
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.domain.auth.UserPendingActivation
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
        is Success -> Response(Status.OK).with(PostCreateUserResponseDTO.lens of pendingUserResult.value.toDto())
        is Failure -> Response(Status.BAD_REQUEST).with(ErrorResponseDTO.lens of ErrorResponseDTO(Status.BAD_REQUEST.description))
    }
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
) {
    companion object {
        val lens = Json.autoBody<PostCreateUserResponseDTO>().toLens()

        fun UserPendingActivation.toDto() = PostCreateUserResponseDTO(
            userId = userId,
            activationToken = activationToken,
        )
    }
}
