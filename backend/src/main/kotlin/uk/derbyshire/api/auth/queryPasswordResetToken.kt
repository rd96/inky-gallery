package uk.derbyshire.api.auth

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.config.Secret
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.auth.QueryPasswordResetTokenResponseDTO.Companion.toDto
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.domain.auth.UserPasswordResetToken
import uk.derbyshire.services.AuthService
import kotlin.time.Instant

fun queryPasswordResetToken(authService: AuthService) = { request: Request ->
    val (passwordResetToken) = QueryPasswordResetTokenRequestDTO.lens(request)

    when (val result = authService.getPasswordResetDetails(passwordResetToken)) {
        is Success -> Response(Status.OK).with(QueryPasswordResetTokenResponseDTO.lens of result.value.toDto())
        is Failure -> Response(Status.NOT_FOUND)
    }
}

private data class QueryPasswordResetTokenRequestDTO(
    val passwordResetToken: Secret,
) {
    companion object {
        val lens = Json.autoBody<QueryPasswordResetTokenRequestDTO>().toLens()
    }
}


private data class QueryPasswordResetTokenResponseDTO(
    val username: String,
    val displayName: String,
    val expiresAt: Instant,
) {
    companion object {
        val lens = Json.autoBody<QueryPasswordResetTokenResponseDTO>().toLens()

        fun UserPasswordResetToken.toDto() = QueryPasswordResetTokenResponseDTO(
            username = username,
            displayName = displayName,
            expiresAt = expiresAt,
        )
    }
}