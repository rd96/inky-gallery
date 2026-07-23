package uk.derbyshire.api.admin

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.admin.GetUserResponseDTO.Companion.toDto
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.api.helpers.Path
import uk.derbyshire.domain.users.ActivationStatus
import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.UserId
import uk.derbyshire.domain.users.UserSummary
import uk.derbyshire.services.UserService
import kotlin.time.Instant

fun getUser(userService: UserService) = { request: Request ->
    val userId = Path.userId(request)
    
    val user = userService.findUser(userId)
    
    if (user != null) Response(Status.OK).with(GetUserResponseDTO.lens of user.toDto())
    else Response(Status.NOT_FOUND)
}

data class GetUserResponseDTO(
    val userId: UserId,
    val username: String,
    val displayName: String,
    val role: Role,
    val activationStatus: ActivationStatus,
    val enabled: Boolean,
    val createdAt: Instant,
) {
    companion object {
        val lens = Json.autoBody<GetUserResponseDTO>().toLens()
        
        fun UserSummary.toDto() = GetUserResponseDTO(
            userId = id,
            username = username,
            displayName = displayName,
            role = role,
            activationStatus = activationStatus,
            enabled = enabled,
            createdAt = createdAt,
        )
    }
}