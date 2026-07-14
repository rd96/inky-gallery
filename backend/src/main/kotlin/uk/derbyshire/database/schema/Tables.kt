package uk.derbyshire.database.schema

import org.jetbrains.exposed.v1.core.Table

class Tables {
    companion object {
        val all = arrayOf<Table>(
            UserTable,
            SessionTable,
        )
    }
}