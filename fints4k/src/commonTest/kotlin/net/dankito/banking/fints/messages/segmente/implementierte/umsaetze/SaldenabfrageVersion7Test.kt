package net.dankito.banking.fints.messages.segmente.implementierte.umsaetze

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import net.dankito.banking.fints.FinTsTestBase
import ch.tutteli.atrium.api.verbs.expect
import kotlin.test.Test


class SaldenabfrageVersion7Test : FinTsTestBase() {

    @Test
    fun format_NotAllAccounts() {

        // given
        val underTest = SaldenabfrageVersion7(3, Account, Bank, false)

        // when
        val result = underTest.format()

        // then
        expect(result).toBe("HKSAL:3:7+$Iban::$CustomerId::280:$BankCode+N")
    }

    @Test
    fun format_AllAccounts() {

        // given
        val underTest = SaldenabfrageVersion7(3, Account, Bank, true)

        // when
        val result = underTest.format()

        // then
        expect(result).toBe("HKSAL:3:7+$Iban::$CustomerId::280:$BankCode+J")
    }

}