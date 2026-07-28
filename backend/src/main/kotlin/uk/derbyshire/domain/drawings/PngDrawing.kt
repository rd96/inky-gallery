package uk.derbyshire.domain.drawings

data class PngDrawing(
    val widthPx: Int,
    val heightPx: Int,
    val data: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PngDrawing

        if (widthPx != other.widthPx) return false
        if (heightPx != other.heightPx) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = widthPx
        result = (31 * result) + heightPx
        result = (31 * result) + data.contentHashCode()
        return result
    }
}