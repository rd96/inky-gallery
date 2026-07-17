package uk.derbyshire.api.auth

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.config.Secret
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.auth.QueryActivationTokenResponseDTO.Companion.toDto
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.domain.auth.UserActivationToken
import uk.derbyshire.services.AuthService
import kotlin.time.Instant

fun queryActivationToken(authService: AuthService) = { request: Request ->
    val activationTokenRequest = QueryActivationTokenRequestDTO.lens(request)

    val result = authService.getActivationDetails(
        activationTokenRequest.activationToken,
    )

    when (result) {
        is Success -> Response(Status.OK).with(QueryActivationTokenResponseDTO.lens of result.value.toDto())
        is Failure -> Response(Status.NOT_FOUND)
    }
}

private data class QueryActivationTokenRequestDTO(
    val activationToken: Secret,
) {
    companion object {
        val lens = Json.autoBody<QueryActivationTokenRequestDTO>().toLens()
    }
}

private data class QueryActivationTokenResponseDTO(
    val username: String,
    val displayName: String,
    val expiresAt: Instant,
) {
    companion object {
        val lens = Json.autoBody<QueryActivationTokenResponseDTO>().toLens()

        fun UserActivationToken.toDto() = QueryActivationTokenResponseDTO(
            username = username,
            displayName = displayName,
            expiresAt = expiresAt,
        )
    }
}