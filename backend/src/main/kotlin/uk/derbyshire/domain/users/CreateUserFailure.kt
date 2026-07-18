package uk.derbyshire.domain.users

enum class CreateUserFailure(val description: String) {
    INVALID_USERNAME("Invalid username"),
    INVALID_DISPLAY_NAME("Invalid display name"),
    USERNAME_ALREADY_IN_USE("User already exists"),
}