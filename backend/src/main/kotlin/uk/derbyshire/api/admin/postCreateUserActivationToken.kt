package uk.derbyshire.api.admin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.admin.PostCreateUserActivationTokenResponseDTO.Companion.toDto
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.Path
import uk.derbyshire.domain.auth.UserPendingActivation
import uk.derbyshire.domain.users.UserId
import uk.derbyshire.services.AuthService

fun postCreateUserActivationToken(authService: AuthService) = { request: Request ->
    val user = CurrentUser(request)
    val userId = Path.userId(request)

    val pendingUserResult = authService.createUserActivationToken(
        userId = userId,
        createdBy = user.userId,
    )

    when (pendingUserResult) {
        is Success -> Response(Status.OK).with(PostCreateUserActivationTokenResponseDTO.lens of pendingUserResult.value.toDto())
        is Failure -> Response(Status.BAD_REQUEST).with(ErrorResponseDTO.lens of ErrorResponseDTO(Status.BAD_REQUEST.description))
    }
}

data class PostCreateUserActivationTokenResponseDTO(
    val userId: UserId,
    val activationToken: String,
) {
    companion object {
        val lens = Json.autoBody<PostCreateUserActivationTokenResponseDTO>().toLens()

        fun UserPendingActivation.toDto() = PostCreateUserActivationTokenResponseDTO(
            userId = userId,
            activationToken = activationToken,
        )
    }
}