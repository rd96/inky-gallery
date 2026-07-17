package uk.derbyshire.domain.auth

enum class ActivationFailure(val description: String) {
    USER_NOT_FOUND("User not found"),
    USER_ALREADY_ACTIVATED("User already activated"),
    USER_DISABLED("User is disabled"),
    INVALID_ACTIVATION_TOKEN("Invalid activation token"),
    PASSWORD_INVALID("Password is invalid"),
}