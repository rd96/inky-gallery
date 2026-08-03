package uk.derbyshire.domain.drawings

enum class SaveDrawingFailure(val description: String) {
    UNSUPPORTED_FORMAT("Unsupported drawing format"),
    IMAGE_TOO_LARGE("Image too large"),
    CANVAS_NOT_FOUND("Canvas not found"),
}