package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.FinTsTestBase
import net.dankito.fints.model.MessageBaseData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class SignaturkopfTest : FinTsTestBase() {

    @Test
    fun format() {

        // given
        val controlReference = "1902675680"

        val underTest = PinTanSignaturkopf(2, MessageBaseData(Bank, Customer, Product),
            controlReference, Date, Time)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("HNSHK:2:4+PIN:2+${SecurityFunction.code}+$controlReference+1+1+1::0+1+1:$Date:$Time+1:999:1+6:10:16+280:$BankCode:$CustomerId:S:0:0")
    }

}