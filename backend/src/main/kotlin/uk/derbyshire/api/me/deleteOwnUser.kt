package uk.derbyshire.api.me

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.cookie
import uk.derbyshire.ServerConfig
import uk.derbyshire.api.filters.CurrentUser
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.toErrorResponseDTO
import uk.derbyshire.api.helpers.expiredSessionCookie
import uk.derbyshire.domain.users.Role
import uk.derbyshire.services.AuthService

fun deleteOwnUser(authService: AuthService, serverConfig: ServerConfig) = request@{ request: Request ->
    val currentUser = CurrentUser(request)

    if (currentUser.role == Role.ADMIN) return@request "Cannot disable admin, please downgrade first".toErrorResponseDTO()

    authService.disableUserAndRevokeSessions(currentUser.userId)

    Response(Status.NO_CONTENT).cookie(expiredSessionCookie(serverConfig.secureSessionCookies))
}