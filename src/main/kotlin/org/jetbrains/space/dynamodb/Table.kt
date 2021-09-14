package org.jetbrains.space.dynamodb

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Path

data class Table(
    val columnsNames: List<String>,
    val values: List<List<String>>
) {
    init {
        for (columnName in columnsNames) {
            require(columnName.isNotEmpty()) { "Columns names cannot be empty." }
        }
        for (row in values) {
            require(row.size == columnsNames.size) {
                "Rows have different sizes or it's different from columns number."
            }
        }
    }

    companion object {
        fun parseFromCsv(path: Path): Table {
            val parser = CSVParser.parse(path, Charset.defaultCharset(), CSVFormat.DEFAULT)
            val records = try {
                parser.records
            } catch (e: IOException) {
                throw IllegalArgumentException("Couldn't parse CSV file from '$path'", e)
            }
            require(records.isNotEmpty()) { "CSV file must contain at lease one row." }
            return Table(
                records.first().toList(),
                records.drop(1).map { it.toList() }
            )
        }
    }
}
