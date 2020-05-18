package net.dankito.banking.fints.messages.segmente.implementierte

import net.dankito.banking.fints.FinTsTestBase
import net.dankito.banking.fints.model.MessageBaseData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class VerschluesselungskopfTest : FinTsTestBase() {

    @Test
    fun format() {

        // given

        val underTest = PinTanVerschluesselungskopf(MessageBaseData(Bank, Customer, Product), Date, Time)

        // when
        val result = underTest.format()

        // then
        assertThat(normalizeBinaryData(result)).isEqualTo("HNVSK:998:3+PIN:2+998+1+1::0+1:$Date:$Time+2:16:14:@8@        :5:1+280:$BankCode:$CustomerId:V:0:0+0")
    }

}