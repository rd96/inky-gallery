package uk.derbyshire.domain.users

enum class CreateAdminFailure {
    ADMIN_ALREADY_EXISTS,
    INVALID_USERNAME,
    INVALID_DISPLAY_NAME,
    INVALID_PASSWORD,
    USERNAME_TAKEN,
}