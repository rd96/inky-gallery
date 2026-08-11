package uk.derbyshire.domain.messages

import dev.forkhandles.result4k.Success
import uk.derbyshire.services.MessageService.Companion.MAX_MESSAGE_LENGTH

enum class SendMessageFailure(val description: String) {
    MESSAGE_TOO_LONG("Message is too long, must be no more than $MAX_MESSAGE_LENGTH characters"),
    UNSUPPORTED_CHARACTERS_IN_MESSAGE("Limited character set for InkyFrame, stick to basic characters"),
    CANVAS_NOT_FOUND("Canvas not found"),
    RECIPIENT_NOT_FOUND("Recipient not found"),
    CANVAS_NOT_FINISHED("Canvas not finished"),
}