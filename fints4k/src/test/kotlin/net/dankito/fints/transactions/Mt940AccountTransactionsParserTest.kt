package net.dankito.fints.transactions

import net.dankito.fints.FinTsTestBase
import net.dankito.fints.model.AccountData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class Mt940AccountTransactionsParserTest : FinTsTestBase() {


    private val underTest = Mt940AccountTransactionsParser()


    @Test
    fun parseTransactions() {

        // given
        val transactionsString = loadTestFile(TransactionsMt940Filename)


        // when
        val result = underTest.parseTransactions(transactionsString, AccountData())


        // then
        assertThat(result).hasSize(55)
    }

}