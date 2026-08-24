package uk.derbyshire.domain.auth

enum class RevokeUserActivationTokensFailure(val description: String) {
    USER_NOT_FOUND("User not found"),
}