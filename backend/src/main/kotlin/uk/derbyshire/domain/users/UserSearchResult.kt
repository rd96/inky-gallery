package uk.derbyshire.domain.users

data class UserSearchResult(
    val users: List<UserSummary>,
    val resultCount: Long,
)