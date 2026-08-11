package uk.derbyshire.services

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.repositories.CanvasRepository
import uk.derbyshire.database.repositories.ConnectionRepository
import uk.derbyshire.database.repositories.MessageRepository
import uk.derbyshire.domain.canvases.CanvasId
import uk.derbyshire.domain.canvases.CanvasStatus
import uk.derbyshire.domain.messages.SendMessageFailure
import uk.derbyshire.domain.users.UserId

class MessageService(
    val messageRepository: MessageRepository,
    val connectionRepository: ConnectionRepository,
    val canvasRepository: CanvasRepository,
    val context: DatabaseContext,
) {
    fun sendMessage(fromUserId: UserId, toUserId: UserId, canvasId: CanvasId, message: String?, showName: Boolean): Result4k<Unit, SendMessageFailure> {
        val normalisedMessage = message?.let(::normaliseAndValidateMessage)?.onFailure { return it }

        return context.transaction {
            if (!connectionRepository.checkUserHasActiveRecipient(fromUserId, toUserId)) return@transaction Failure(SendMessageFailure.RECIPIENT_NOT_FOUND)
            val canvas = canvasRepository.getCanvas(fromUserId, canvasId) ?: return@transaction Failure(SendMessageFailure.CANVAS_NOT_FOUND)

            if (canvas.status != CanvasStatus.FINISHED) return@transaction Failure(SendMessageFailure.CANVAS_NOT_FINISHED)

            messageRepository.insertMessage(fromUserId, toUserId, canvasId, normalisedMessage, showName).asSuccess()
        }
    }

    companion object {
        // supported character set in InkyFrame standard bit-font, may need review later to manage per device
        // InkyFrame will ignore these characters, so might not be necessary or could show a warning to the user
        // rather than failing.
        private val supportedCharacters = Regex(
            """^[\u0020-\u007E\u00A0\u00A3\u00A5\u00A9\u00B0\u00C0-\u00CF\u00D1-\u00D6\u00D8-\u00EF\u00F1-\u00F6\u00F8-\u00FF]*$"""
        )
        const val MAX_MESSAGE_LENGTH = 100

        fun normaliseAndValidateMessage(message: String): Result4k<String?, SendMessageFailure> {
            val normalisedMessage = message.trim()

            return if (normalisedMessage.isEmpty()) Success(null)
            else if (normalisedMessage.length > MAX_MESSAGE_LENGTH) Failure(SendMessageFailure.MESSAGE_TOO_LONG)
            else if (supportedCharacters.matches(normalisedMessage)) Success(normalisedMessage)
            else Failure(SendMessageFailure.UNSUPPORTED_CHARACTERS_IN_MESSAGE)
        }
    }
}