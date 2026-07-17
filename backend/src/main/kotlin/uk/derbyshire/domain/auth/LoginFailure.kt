package uk.derbyshire.domain.auth

enum class LoginFailure(val description: String) {
    USER_DISABLED("User is disabled"),
    USER_PENDING_ACTIVATION("User is pending activation"),
    PASSWORD_INCORRECT("Password is incorrect"),
    USER_NOT_FOUND("User not found"),
}