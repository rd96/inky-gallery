package uk.derbyshire.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import uk.derbyshire.DatabaseConfig
import java.io.Closeable
import org.jetbrains.exposed.v1.jdbc.Database as ExposedDatabase
import org.jetbrains.exposed.v1.jdbc.transactions.transaction as exposedTransaction

class DatabaseContext(config: DatabaseConfig): Closeable {
    val dataSource: HikariDataSource = createDataSource(config)

    val database = ExposedDatabase.connect(dataSource)

    fun <T> transaction(block: () -> T): T =
        exposedTransaction(db = database) {
            block()
        }

    override fun close() {
        dataSource.close()
    }

    private fun createDataSource(config: DatabaseConfig): HikariDataSource =
        config.username.use { username ->
            config.password.use { password ->
                val hikariConfig = HikariConfig().apply {
                    jdbcUrl = config.url.toString()
                    driverClassName = "org.postgresql.Driver"

                    this.username = username
                    this.password = password

                    maximumPoolSize = 10
                    isAutoCommit = false
                    transactionIsolation = "TRANSACTION_READ_COMMITTED"
                    leakDetectionThreshold = 30000

                    validate()
                }

                HikariDataSource(hikariConfig)
            }
        }
}