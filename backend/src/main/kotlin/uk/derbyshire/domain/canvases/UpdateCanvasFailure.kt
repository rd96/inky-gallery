package uk.derbyshire.domain.canvases

enum class UpdateCanvasFailure(val description: String) {
    DRAWINGS_MISMATCH("Drawing do not match the current drawings in canvas"),
    CANNOT_REORDER_SINGLE_DRAWING_CANVAS("Cannot reorder a single drawing in the canvas"),
    CANVAS_NOT_IN_DRAFT("Cannot reorder a canvas not in draft"),
    CANVAS_NOT_FOUND("Canvas not found"),

}
