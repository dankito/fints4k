package net.dankito.banking.fints.messages.segmente.implementierte.umsaetze

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import net.dankito.banking.fints.FinTsTestBase
import kotlin.test.Test


class SaldenabfrageVersion5Test : FinTsTestBase() {

    @Test
    fun format_NotAllAccounts() {

        // given
        val underTest = SaldenabfrageVersion5(3, Account, false)

        // when
        val result = underTest.format()

        // then
        expect(result).toBe("HKSAL:3:5+$CustomerId::280:$BankCode+N")
    }

    @Test
    fun format_AllAccounts() {

        // given
        val underTest = SaldenabfrageVersion5(3, Account, true)

        // when
        val result = underTest.format()

        // then
        expect(result).toBe("HKSAL:3:5+$CustomerId::280:$BankCode+J")
    }

}