package net.dankito.banking.fints.messages.segmente.implementierte.umsaetze

import net.dankito.banking.fints.FinTsTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class SaldenabfrageVersion7Test : FinTsTestBase() {

    @Test
    fun format_NotAllAccounts() {

        // given
        val underTest = SaldenabfrageVersion7(3, Account, Bank, false)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("HKSAL:3:7+$Iban::$CustomerId::280:$BankCode+N")
    }

    @Test
    fun format_AllAccounts() {

        // given
        val underTest = SaldenabfrageVersion7(3, Account, Bank, true)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("HKSAL:3:7+$Iban::$CustomerId::280:$BankCode+J")
    }

}