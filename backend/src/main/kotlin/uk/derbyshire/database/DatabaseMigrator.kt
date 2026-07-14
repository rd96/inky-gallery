package uk.derbyshire.database

import org.flywaydb.core.Flyway
import javax.sql.DataSource

class DatabaseMigrator(
    private val dataSource: DataSource,
) {
    fun migrate() {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }
}