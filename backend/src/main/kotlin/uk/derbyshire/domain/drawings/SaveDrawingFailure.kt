package uk.derbyshire.domain.drawings

enum class SaveDrawingFailure(val description: String) {
    UNSUPPORTED_FORMAT("Unsupported drawing format"),
    INVALID_IMAGE_SIZE("Image is the wrong size for this canvas"),
    CANVAS_NOT_FOUND("Canvas not found"),
    CANVAS_IS_NOT_IN_DRAFT("Canvas must be a draft to add drawings"),
    SINGLE_CANVAS_CAN_ONLY_HAVE_ONE_DRAWING("Single canvas can only have one drawing"),
}