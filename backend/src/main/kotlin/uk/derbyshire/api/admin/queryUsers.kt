package uk.derbyshire.api.admin

import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import uk.derbyshire.api.admin.QueryUsersResponseDTO.Companion.toQueryUsersResponseDto
import uk.derbyshire.api.admin.UserResponseDTO.Companion.toUserResponseDto
import uk.derbyshire.api.helpers.Json
import uk.derbyshire.domain.users.Role
import uk.derbyshire.domain.users.UserSearchResult
import uk.derbyshire.domain.users.UserStatus
import uk.derbyshire.domain.users.UserSummary
import uk.derbyshire.services.UserService
import kotlin.time.Instant
import kotlin.uuid.Uuid

fun queryUsers(userService: UserService) = { request: Request ->
    val searchRequest = QueryUsersRequestDTO.lens(request)

    val results = userService.searchAllUsers(
        searchRequest.nameSearch,
        searchRequest.role,
        searchRequest.status,
        searchRequest.page,
    ).toQueryUsersResponseDto()

    Response(Status.OK).with(QueryUsersResponseDTO.lens of results)
}

private data class QueryUsersRequestDTO(
    val nameSearch: String? = null,
    val role: Role? = null,
    val status: UserStatus? = null,
    val page: Int = 1,
) {
    companion object {
        val lens = Json.autoBody<QueryUsersRequestDTO>().toLens()
    }
}

private data class QueryUsersResponseDTO(
    val users: List<UserResponseDTO>,
    val totalCount: Long,
) {
    companion object {
        val lens = Json.autoBody<QueryUsersResponseDTO>().toLens()

        fun UserSearchResult.toQueryUsersResponseDto() = QueryUsersResponseDTO(
            users = users.map { it.toUserResponseDto() },
            totalCount = totalCount,
        )
    }
}

private data class UserResponseDTO(
    val userId: Uuid,
    val username: String,
    val displayName: String,
    val role: Role,
    val status: UserStatus,
    val createdAt: Instant,
) {
    companion object {
        fun UserSummary.toUserResponseDto() = UserResponseDTO(
            userId = id,
            username = username,
            displayName = displayName,
            role = role,
            status = status,
            createdAt = createdAt,
        )
    }
}


