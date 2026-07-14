package uk.derbyshire.tooling

import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import uk.derbyshire.DatabaseConfig
import uk.derbyshire.Environment
import uk.derbyshire.database.DatabaseContext
import uk.derbyshire.database.schema.Tables

object GenerateDatabaseMigration {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = DatabaseConfig.from(Environment.source)
        val database = DatabaseContext(config)

        val statements = database.transaction {
            MigrationUtils.statementsRequiredForDatabaseMigration(
                *Tables.all,
            )
        }

        if (statements.isEmpty()) {
            println("No database migration required.")
            return
        }

        statements.forEach { statement ->
            println(statement)
        }
    }
}