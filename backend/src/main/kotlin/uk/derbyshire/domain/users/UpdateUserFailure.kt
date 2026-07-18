package uk.derbyshire.domain.users

enum class UpdateUserFailure(val description: String) {
    USER_NOT_FOUND("User not found."),
    USERNAME_ALREADY_IN_USE("Username already exists."),
    INVALID_USERNAME("Username is invalid."),
    INVALID_DISPLAY_NAME("Invalid display name"),
    CANNOT_DEMOTE_LAST_ADMIN("Cannot demote last admin."),
}