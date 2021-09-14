package org.jetbrains.space.dynamodb

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Path

/**
 * Represents a simple table with [columnsNames] and its [values].
 * The size of each row in [values] must match with [columnsNames] length.
 * Also, size of [columnsNames] must be non-zero.
 */
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
        /**
         * Tries to parse CSV file from [path].
         * @throws IllegalArgumentException if the file cannot be parsed or contains no rows.
         */
        fun parseFromCsv(path: Path): Table {
            val parser = CSVParser.parse(path, Charset.defaultCharset(), CSVFormat.DEFAULT)
            val records = try {
                parser.records
            } catch (e: IOException) {
                throw IllegalArgumentException("Couldn't parse CSV file from '$path'", e)
            }
            require(records.isNotEmpty()) { "CSV file must contain at least one row." }
            return Table(
                records.first().toList(),
                records.drop(1).map { it.toList() }
            )
        }
    }
}
