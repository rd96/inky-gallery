package uk.derbyshire.api.admin

import org.http4k.core.Method
import org.http4k.routing.bind
import org.http4k.routing.routes
import uk.derbyshire.services.AuthService
import uk.derbyshire.services.UserService

fun adminRoutes(userService: UserService, authService: AuthService) = routes(
    "/users" bind Method.QUERY to queryUsers(userService),
    "/users" bind Method.POST to postCreateUser(authService),
    "/users/{userId}" bind Method.PUT to patchUser(),

    "/users/{userId}/activation-links" bind Method.POST to postCreateUserActivationLink(),
    "/users/{userId}/activation-links" bind Method.DELETE to deleteUserActivationLink(),

    "/user-links" bind Method.POST to postCreateUserLink(),
    "/user-links/{linkId}" bind Method.DELETE to deleteUserLink(),

)