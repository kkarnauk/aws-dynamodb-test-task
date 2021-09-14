package org.jetbrains.space.dynamodb

import java.nio.file.Path

/**
 * Represents options for work.
 */
data class Options(
    val database: DatabaseInfo,
    val table: TableInfo,
    val csvPath: Path
) {
    /**
     * Information to connect a database.
     */
    data class DatabaseInfo(
        val url: String,
        val user: String,
        val password: String
    )

    /**
     * Information about a table inside a database.
     */
    data class TableInfo(
        val name: String,
        val schema: String
    )

    companion object {
        /**
         * Parses command line arguments and reads the left information from a console.
         * All info cannot be passed as arguments for security reasons.
         *
         * Format of args: `path/to/csv/file table_name schema_name`
         *
         * After that, you should enter an URL, username and password for your [DatabaseInfo].
         */
        fun parseAndReadOptions(commandArgs: List<String>): Options {
            require(commandArgs.size == 3) {
                "You have to provide exactly 3 arguments: path to CSV, table name, schema name."
            }
            return Options(
                readDatabase(),
                TableInfo(commandArgs[1], commandArgs[2]),
                Path.of(commandArgs[0])
            )
        }

        private fun readDatabase(): DatabaseInfo {
            while (true) {
                println("Please, enter your DataBase URL:")
                val url = readLine() ?: continue
                println("Please, enter your user name:")
                val user = readLine() ?: continue
                println("Please, enter your password:")
                val password = System.console()?.readPassword()?.concatToString() ?: readLine() ?: continue
                return DatabaseInfo(url, user, password)
            }
        }
    }
}
