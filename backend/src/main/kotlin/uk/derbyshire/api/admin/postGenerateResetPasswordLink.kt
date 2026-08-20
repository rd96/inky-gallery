package uk.derbyshire.api.admin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.admin.PostGeneratePasswordResetTokenResponseDTO.Companion.toDto
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.PathParams.userId
import uk.derbyshire.domain.auth.PasswordResetToken
import uk.derbyshire.services.AuthService
import kotlin.time.Instant

fun postGeneratePasswordResetToken(authService: AuthService) = { request: Request ->
    val currentUser = CurrentUser(request)
    val userId = userId(request)

    val result = authService.generateResetPasswordToken(
        userId = userId,
        createdBy = currentUser.userId,
    )

    when (result) {
        is Success -> Response(Status.OK).with(PostGeneratePasswordResetTokenResponseDTO.lens of result.value.toDto())
        is Failure -> result.reason.description.toErrorResponseDTO()
    }
}

private data class PostGeneratePasswordResetTokenResponseDTO(
    val passwordResetToken: String,
    val expiresAt: Instant,
) {
    companion object {
        val lens = Json.autoBody<PostGeneratePasswordResetTokenResponseDTO>().toLens()

        fun PasswordResetToken.toDto() = PostGeneratePasswordResetTokenResponseDTO(
            passwordResetToken = resetToken,
            expiresAt = expiresAt,
        )
    }
}