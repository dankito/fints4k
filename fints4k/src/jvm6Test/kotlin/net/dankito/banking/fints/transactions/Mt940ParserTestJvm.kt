package net.dankito.banking.fints.transactions

import net.dankito.banking.fints.FinTsTestBaseJvm
import net.dankito.banking.fints.transactions.mt940.Mt940Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class Mt940ParserTestJvm : FinTsTestBaseJvm() {

    private val underTest = Mt940Parser()


    @Test
    fun parseTransactions() {

        // given
        val transactionsString = loadTestFile(TransactionsMt940Filename)


        // when
        val result = underTest.parseMt940String(transactionsString)


        // then
        assertThat(result).hasSize(32)
    }

}