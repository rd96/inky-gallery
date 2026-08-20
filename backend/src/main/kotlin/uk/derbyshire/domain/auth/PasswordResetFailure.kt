package uk.derbyshire.domain.auth

enum class PasswordResetFailure(val description: String) {
    INVALID_TOKEN("Invalid token"),
    USER_NOT_FOUND("User not found"),
    USER_DISABLED("User disabled"),
    PASSWORD_INVALID("Password is invalid"),

}