package uk.derbyshire.api.filters

import org.http4k.core.Filter
import org.http4k.core.with
import org.http4k.lens.Header
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.util.UUID

class RequestLogger {
    private val logger = LoggerFactory.getLogger(RequestLogger::class.java)

    fun requestIdFilter() = Filter { next ->
        { request ->
            val requestId = REQUEST_ID_HEADER(request) ?: UUID.randomUUID().toString()
            MDC.put(REQUEST_ID_MDC_KEY, requestId)
            try {
                next(request).with(REQUEST_ID_HEADER of requestId)
            } finally {
                MDC.remove(REQUEST_ID_MDC_KEY)
            }
        }
    }

    fun requestLoggingFilter() = Filter { next ->
        { request ->
            val start = System.currentTimeMillis()
            val response = next(request)
            val duration = System.currentTimeMillis() - start
            logger.info("{} {} -> {} ({}ms)", request.method, request.uri.path, response.status.code, duration)
            response
        }
    }

    companion object {
        private val REQUEST_ID_HEADER = Header.optional("X-Request-Id")
        const val REQUEST_ID_MDC_KEY = "requestId"
    }
}


