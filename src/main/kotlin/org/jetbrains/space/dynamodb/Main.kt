package org.jetbrains.space.dynamodb

fun main(args: Array<String>) {
    val options = Options.parseAndReadOptions(args.toList())
    val table = Table.parseFromCsv(options.csvPath)
    HSQLDatabase.connect(options.database)?.use { database ->
        database.createTableOrAppend(options.table, table)
    } ?: println("Couldn't connect to your database.")
}
