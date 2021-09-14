package org.jetbrains.space.dynamodb

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Represents a simple database that can create new tables and append values to them.
 * @see Table
 */
interface Database {
    /**
     * Creates a new table from [table] with [Table.columnsNames] as columns and [Table.values] as values.
     * Takes info about a name and a schema of a table from [info].
     *
     * @throws RuntimeException if a table with the name and schema already exists. // TODO exceptions
     */
    fun createTable(info: Options.TableInfo, table: Table)

    /**
     * Checks if a table with a name and a schema from [info] exists in the database.
     */
    fun tableExists(info: Options.TableInfo): Boolean

    /**
     * Appends values from [table] to a table with a name and a schema from [info].
     * @throws RuntimeException if there is no such table or it doesn't match by columns names with [table].
     */
    fun appendToTable(info: Options.TableInfo, table: Table)

    /**
     * If there is such a table with [info], calls [appendToTable], otherwise, calls [createTable].
     */
    fun createTableOrAppend(info: Options.TableInfo, table: Table) =
        if (tableExists(info)) appendToTable(info, table) else createTable(info, table)
}

/**
 * HSQL-Database that supports only string values up to [MAX_LENGTH] characters.
 */
class HSQLDatabase private constructor(private val connection: Connection) : Database, AutoCloseable {
    override fun close() = connection.close()

    override fun createTable(info: Options.TableInfo, table: Table) {
        require(!tableExists(info)) { "Cannot create a table: it already exists." }
        try {
            val columns = table.columnsNames.joinToString(", ") { "$it varchar($MAX_LENGTH)" }
            connection.prepareStatement(
                "create table ${info.schema}.${info.name} ($columns)"
            ).use { it.execute() }

            doAppendToTable(info, table)
        } catch (e: SQLException) {
            throw RuntimeException("Cannot create a table.", e)
        }
    }

    override fun tableExists(info: Options.TableInfo): Boolean {
        if (!schemaExists(info.schema)) {
            return false
        }
        return try {
            connection.metaData.getTables(null, info.schema, info.name, arrayOf("TABLE")).use {
                it.next()
            }
        } catch (e: SQLException) {
            throw RuntimeException("Cannot query tables.", e)
        }
    }

    override fun appendToTable(info: Options.TableInfo, table: Table) {
        require(getColumnsNames(info) == table.columnsNames) {
            "CSV columns names don't match with columns names in the table."
        }
        doAppendToTable(info, table)
    }

    private fun doAppendToTable(info: Options.TableInfo, table: Table) {
        try {
            val rows = table.values.joinToString(", ") { row ->
                row.joinToString(", ", "(", ")") { "'$it'" }
            }
            connection.prepareStatement(
                "insert into ${info.schema}.${info.name} values $rows"
            ).use { it.execute() }
        } catch (e: SQLException) {
            throw RuntimeException("Cannot append values to a table.", e)
        }
    }

    private fun schemaExists(schema: String): Boolean {
        return try {
            connection.metaData.schemas.use { schemas ->
                schemas.sequenceOfStringValues("TABLE_SCHEM").any { it == schema }
            }
        } catch (e: SQLException) {
            throw RuntimeException("Cannot query schemas names.", e)
        }
    }

    private fun getColumnsNames(info: Options.TableInfo): List<String> {
        return try {
            connection.prepareStatement(
                """
                select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where
                TABLE_SCHEMA = '${info.schema}' and TABLE_NAME = '${info.name}'
            """.trimIndent()
            ).use { statement ->
                statement.executeQuery().use { columns ->
                    columns.sequenceOfStringValues("COLUMN_NAME").toList()
                }
            }
        } catch (e: SQLException) {
            throw RuntimeException("Cannot query columns names.", e)
        }
    }

    companion object {
        init {
            Class.forName("org.hsqldb.jdbc.JDBCDriver")
        }

        /**
         * Max length of string values in a HSQL-Database.
         */
        const val MAX_LENGTH: Int = 255

        /**
         * Connects to a database using [databaseInfo].
         */
        fun connect(databaseInfo: Options.DatabaseInfo): HSQLDatabase? {
            return try {
                DriverManager.getConnection(
                    databaseInfo.url,
                    databaseInfo.user,
                    databaseInfo.password
                )?.let { HSQLDatabase(it) }
            } catch (e: Exception) {
                throw IllegalArgumentException("Couldn't connect to your database.", e)
            }
        }

        private fun ResultSet.sequenceOfStringValues(label: String): Sequence<String> =
            generateSequence { if (next()) getString(label) else null }
    }
}
