package uk.derbyshire.domain.canvases

enum class CompleteCanvasFailure(val description: String) {
    CANVAS_STACK_NEEDS_MORE_DRAWINGS("Canvas stack needs more than one drawing"),
    CANVAS_HAS_TOO_MANY_DRAWINGS("Single canvas must only have one drawing"),
    CANVAS_IS_BLANK("Canvas has no drawings"),
    CANVAS_NOT_IN_DRAFT("Canvas is not in draft state"),
    CANVAS_NOT_FOUND("Canvas not found"),
}