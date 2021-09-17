package org.jetbrains.space.dynamodb

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

class TableParserTest {
    private val tempDir = createTempDirectory()

    @Test
    fun `test simple csv`() = doSuccessTest(
        """
        a,b,c,d
        1,2,3,4
        val1,val2,val3,val4
    """.trimIndent(),
        Table(
            listOf("a", "b", "c", "d"),
            listOf(
                listOf("1", "2", "3", "4"),
                listOf("val1", "val2", "val3", "val4"),
            )
        )
    )

    @Test
    fun `test csv with spaces`() = doSuccessTest(
        """
            a b, a c ,   hello
            1,2 , 3
        """.trimIndent(),
        Table(
            listOf("a b", " a c ", "   hello"),
            listOf(
                listOf("1", "2 ", " 3")
            )
        )
    )

    @Test
    fun `test csv with commas`() = doSuccessTest(
        """
            "a,aa,,",hello,"a,,a"
            one,"two,,",three
            1,2,3
            kek,kek2,"ke,,k3"
        """.trimIndent(),
        Table(
            listOf("a,aa,,", "hello", "a,,a"),
            listOf(
                listOf("one", "two,,", "three"),
                listOf("1", "2", "3"),
                listOf("kek", "kek2", "ke,,k3")
            )
        )
    )

    @Test
    fun `test csv with quotes`() = doSuccessTest(
        """
            "a""hello""b","another"," ""third"" 2"
            value,value," ""value"" ""value"" ""value"" "
        """.trimIndent(),
        Table(
            listOf("a\"hello\"b", "another", " \"third\" 2"),
            listOf(
                listOf("value", "value", " \"value\" \"value\" \"value\" ")
            )
        )
    )

    @Test
    fun `test csv with quotes and commas`() = doSuccessTest(
        """
            a,"b,""bb,,bb"" "
            "value,,"," "" ,"
        """.trimIndent(),
        Table(
            listOf("a", "b,\"bb,,bb\" "),
            listOf(
                listOf("value,,", " \" ,")
            )
        )
    )

    @Test
    fun `test csv incorrect format`() {
        doFailTest<IllegalArgumentException>("")
        doFailTest<IllegalArgumentException>(
            """
                a,b
                a,b,c
            """.trimIndent()
        )
        doFailTest<IllegalArgumentException>(
            """
                a,b,c
                a,b
            """.trimIndent()
        )
        doFailTest<IllegalArgumentException>(
            """
                a,b,c,"a
                a,b,c,d
            """.trimIndent()
        )
        doFailTest<IllegalArgumentException>(
            """
                a,b,c,""d""
                a,b,c,d
            """.trimIndent()
        )
        doFailTest<IllegalArgumentException>(
            """
                a,b,,c
                a,b,c,d
            """.trimIndent()
        )
        doFailTest<IllegalArgumentException>(
            """
                a,"" , "",b
                a,b,c
            """.trimIndent()
        )
    }

    private fun getPath(filename: String): Path = tempDir.resolve(filename)

    private fun doSuccessTest(csvContent: String, expected: Table, filename: String = "Test.csv") =
        doTest(csvContent, filename) { assertEquals(expected, this) }

    private inline fun <reified E : Exception> doFailTest(csvContent: String, filename: String = "Test.csv") =
        assertThrows<E> { doTest(csvContent, filename) { } }

    private fun doTest(csvContent: String, filename: String, check: Table.() -> Unit) {
        val path = getPath(filename)
        path.writeText(csvContent)
        Table.parseFromCsv(path).check()
    }
}
