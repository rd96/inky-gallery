package uk.derbyshire.api.helpers

import org.http4k.core.ContentType
import org.http4k.core.Request
import org.http4k.lens.contentType

object RequestMediaChecks {
    fun Request.hasContentType(contentType: ContentType) = contentType()?.equalsIgnoringDirectives(contentType) == true
    fun Request.readBodyUpTo(maxBytes: Int): ByteArray? {
        val knownLength = body.length

        if (knownLength != null && knownLength > maxBytes.toLong()) return null

        val bytes = body.stream.use { input ->
            // let's read one more than our max
            input.readNBytes(maxBytes + 1)
        }

        return if (bytes.size > maxBytes) null
        else bytes
    }
}