package uk.derbyshire.domain.devices

@JvmInline
value class HexColour private constructor(
    val value: String,
) {
    companion object {
        // only matches #RRGGBB format
        private val pattern = Regex("^#[0-9A-F]{6}$")

        fun parse(value: String): HexColour {
            val normalised = value.uppercase()

            require(pattern.matches(normalised)) {
                "Invalid hex colour: $value"
            }

            return HexColour(normalised)
        }
    }

    override fun toString(): String = value
}