package uk.derbyshire.api.filters

import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.cookie
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestKey
import uk.derbyshire.domain.auth.AuthenticatedDevice
import uk.derbyshire.domain.auth.AuthenticatedUser
import uk.derbyshire.domain.users.Role
import uk.derbyshire.services.AuthService

val CurrentUser = RequestKey.required<AuthenticatedUser>("currentUser")
val DeviceUser = RequestKey.required<AuthenticatedDevice>("deviceUser")

class AuthChecker(
    private val authService: AuthService,
) {
    fun requireUser(): Filter = Filter { next ->
        { request ->
            val token = request.cookie("session")?.value

            val user = token
                ?.let { authService.authenticateSession(it) }

            if (user == null) Response(Status.UNAUTHORIZED)
            else next(request.with(CurrentUser of user))
        }
    }

    fun requireAdmin(): Filter =
        requireUser().then(
            Filter { next ->
                { request ->
                    val user = CurrentUser(request)

                    if (user.role != Role.ADMIN) Response(Status.FORBIDDEN)
                    else next(request)
                }
            }
        )

    fun requireDevice() = ServerFilters.BearerAuth(DeviceUser) { apiKey ->
        authService.authenticateApiKey(apiKey)
    }
}