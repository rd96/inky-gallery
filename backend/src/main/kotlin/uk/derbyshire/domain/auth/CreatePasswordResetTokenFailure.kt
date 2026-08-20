package uk.derbyshire.domain.auth

enum class CreatePasswordResetTokenFailure(val description: String) {
    USER_NOT_FOUND("User not found"),
    USER_NOT_ACTIVATED("User not activated"),
    USER_NOT_ENABLED("User not enabled"),

}