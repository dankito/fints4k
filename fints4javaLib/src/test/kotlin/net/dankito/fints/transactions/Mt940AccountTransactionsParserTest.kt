package net.dankito.fints.transactions

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class Mt940AccountTransactionsParserTest {

    companion object {
        const val TestFilesFolderName = "test_files/"

        const val TransactionsMt940FileRelativePath = TestFilesFolderName + "TransactionsMt940.txt" // TODO: place in common file
    }


    private val underTest = Mt940AccountTransactionsParser()


    @Test
    fun parseTransactions() {

        // given
        val fileStream = Mt940ParserTest::class.java.classLoader.getResourceAsStream(TransactionsMt940FileRelativePath)
        val transactionsString = fileStream.reader().readText()


        // when
        val result = underTest.parseTransactions(transactionsString)


        // then
        assertThat(result).hasSize(55)
    }

}