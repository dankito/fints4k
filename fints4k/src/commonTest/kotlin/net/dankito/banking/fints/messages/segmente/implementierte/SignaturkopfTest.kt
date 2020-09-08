package net.dankito.banking.fints.messages.segmente.implementierte

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import net.dankito.banking.fints.FinTsTestBase
import net.dankito.banking.fints.model.MessageBaseData
import ch.tutteli.atrium.api.verbs.expect
import kotlin.test.Test


class SignaturkopfTest : FinTsTestBase() {

    @Test
    fun format() {

        // given
        val controlReference = "1902675680"

        val underTest = PinTanSignaturkopf(2, MessageBaseData(Bank, Product),
            controlReference, Date, Time)

        // when
        val result = underTest.format()

        // then
        expect(result).toBe("HNSHK:2:4+PIN:2+${SecurityFunction.code}+$controlReference+1+1+1::0+1+1:$Date:$Time+1:999:1+6:10:16+280:$BankCode:$CustomerId:S:0:0")
    }

}