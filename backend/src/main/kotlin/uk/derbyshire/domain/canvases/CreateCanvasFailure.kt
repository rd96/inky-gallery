package uk.derbyshire.domain.canvases

import uk.derbyshire.services.CanvasService.Companion.DRAFT_LIMIT

enum class CreateCanvasFailure(val description: String) {
    DEVICE_MODEL_NOT_FOUND("Device model not found"),
    TOO_MANY_DRAFTS("Too many drafts, max is $DRAFT_LIMIT"),
}