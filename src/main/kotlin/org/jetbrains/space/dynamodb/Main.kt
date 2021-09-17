package org.jetbrains.space.dynamodb

fun main(args: Array<String>) {
    val options = Options.parseAndReadOptions(args.toList())
    val table = Table.parseFromCsv(options.csvPath)
    val dbTable = Table(table.columnsNames.map { it.uppercase() }, table.values)
    HSQLDatabase.connect(options.database)?.use { database ->
        database.createTableOrAppend(options.table, dbTable)
    } ?: println("Couldn't connect to your database.")
}
