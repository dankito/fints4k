package net.dankito.banking.fints.messages.segmente.implementierte.umsaetze

import net.dankito.banking.fints.FinTsTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class SaldenabfrageVersion5Test : FinTsTestBase() {

    @Test
    fun format_NotAllAccounts() {

        // given
        val underTest = SaldenabfrageVersion5(3, Account, false)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("HKSAL:3:5+$CustomerId::280:$BankCode+N")
    }

    @Test
    fun format_AllAccounts() {

        // given
        val underTest = SaldenabfrageVersion5(3, Account, true)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("HKSAL:3:5+$CustomerId::280:$BankCode+J")
    }

}