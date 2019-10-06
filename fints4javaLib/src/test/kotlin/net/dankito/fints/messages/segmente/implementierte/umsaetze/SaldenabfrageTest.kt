package net.dankito.fints.messages.segmente.implementierte.umsaetze

import net.dankito.fints.FinTsTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SaldenabfrageTest : FinTsTestBase() {

    @Test
    fun format_NotAllAccounts() {

        // given
        val underTest =
            Saldenabfrage(3, Bank, Customer, false)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("HKSAL:3:5+$CustomerId::280:$BankCode+N")
    }

    @Test
    fun format_AllAccounts() {

        // given
        val underTest =
            Saldenabfrage(3, Bank, Customer, true)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("HKSAL:3:5+$CustomerId::280:$BankCode+J")
    }

}