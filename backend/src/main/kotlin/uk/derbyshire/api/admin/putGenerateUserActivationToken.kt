package uk.derbyshire.api.admin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.admin.PutGenerateUserActivationTokenResponseDTO.Companion.toDto
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.PathParams.userId
import uk.derbyshire.domain.auth.UserPendingActivation
import uk.derbyshire.domain.users.UserId
import uk.derbyshire.services.AuthService
import kotlin.time.Instant

fun putGenerateUserActivationToken(authService: AuthService) = { request: Request ->
    val user = CurrentUser(request)
    val userId = userId(request)

    val pendingUserResult = authService.generateUserActivationToken(
        userId = userId,
        createdBy = user.userId,
    )

    when (pendingUserResult) {
        is Success -> Response(Status.OK).with(PutGenerateUserActivationTokenResponseDTO.lens of pendingUserResult.value.toDto())
        is Failure -> pendingUserResult.reason.description.toErrorResponseDTO()
    }
}

data class PutGenerateUserActivationTokenResponseDTO(
    val userId: UserId,
    val activationToken: String,
    val expiresAt: Instant,
) {
    companion object {
        val lens = Json.autoBody<PutGenerateUserActivationTokenResponseDTO>().toLens()

        fun UserPendingActivation.toDto() = PutGenerateUserActivationTokenResponseDTO(
            userId = userId,
            activationToken = activationToken,
            expiresAt = expiresAt,
        )
    }
}