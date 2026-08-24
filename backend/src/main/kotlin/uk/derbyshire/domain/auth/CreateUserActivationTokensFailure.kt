package uk.derbyshire.domain.auth

enum class CreateUserActivationTokensFailure(val description: String) {
    USER_NOT_FOUND("User not found"),
    USER_NOT_PENDING("User not pending"),
    USER_NOT_ENABLED("User not enabled"),
}