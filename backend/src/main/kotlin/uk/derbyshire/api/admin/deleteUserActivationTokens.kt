package uk.derbyshire.api.admin

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.filters.ErrorResponseDTO
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.PathParams
import uk.derbyshire.services.AuthService

fun deleteUserActivationTokens(authService: AuthService) = { request: Request ->
    val userId = PathParams.userId(request)

    val result = authService.revokeUserActivationTokens(
        userId = userId,
    )

    when (result) {
        is Success -> Response(Status.NO_CONTENT)
        is Failure -> result.reason.description.toErrorResponseDTO()
    }
}

