package uk.derbyshire

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("uk.derbyshire.Main")

fun main() {
    try {
        val application = Application()
        application.start()
    } catch (e: Exception) {
        logger.error("Error starting server", e)
    }
}
