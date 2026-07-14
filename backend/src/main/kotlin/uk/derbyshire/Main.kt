package uk.derbyshire

fun main() {
    try {
        val application = Application()
        application.start()
    } catch (e: Exception) {
        println("Error starting server: ${e.message}")
    }
}
