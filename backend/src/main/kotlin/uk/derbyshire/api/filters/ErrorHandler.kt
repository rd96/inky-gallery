package uk.derbyshire.api.filters

import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.slf4j.LoggerFactory
import uk.derbyshire.api.filters.ErrorResponseDTO.Companion.lens
import uk.derbyshire.api.helpers.Json

class ErrorHandler {
    private val logger = LoggerFactory.getLogger(ErrorHandler::class.java)

    fun catchUnexpectedErrors(): Filter = { next ->
        { request ->
            try {
                next(request)
            } catch (exception: Exception) {
                logger.error(
                    "Unhandled exception processing ${request.method} ${request.uri.path}",
                    exception,
                )

                Response(Status.INTERNAL_SERVER_ERROR).with(
                    lens of ErrorResponseDTO(Status.INTERNAL_SERVER_ERROR.description)
                )
            }
        }
    }
}

data class ErrorResponseDTO(
    val error: String,
) {
    companion object {
        val lens = Json.autoBody<ErrorResponseDTO>().toLens()

        fun String.toErrorResponseDTO(status: Status = Status.BAD_REQUEST) =
            Response(status).with(lens of ErrorResponseDTO(this))
    }
}