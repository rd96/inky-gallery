package uk.derbyshire

import java.time.Clock

fun main() {
    try {
        startServer()
    } catch (e: Exception) {
        println("Error starting server: ${e.message}")
    }
}

fun startServer() {
    val env = Environment.fromEnv()
    val clock = Clock.systemUTC()

    val server = Server(env, clock)

    server.start()
}