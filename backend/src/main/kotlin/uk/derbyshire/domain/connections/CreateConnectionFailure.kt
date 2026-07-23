package uk.derbyshire.domain.connections

enum class CreateConnectionFailure(val description: String) {
    SENDER_NOT_FOUND("Sender user not found"),
    RECIPIENT_NOT_FOUND("Recipient user not found"),
    CONNECTION_ALREADY_EXISTS("Connection already exists"),
}