package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.Laenderkennzeichen
import net.dankito.fints.messages.datenelemente.implementierte.signatur.IdentifizierungDerPartei
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerschluesselungskopfTest {

    @Test
    fun format() {

        // given
        val partyIdentification = IdentifizierungDerPartei.SynchronizingCustomerSystemId
        val date = 20191002
        val time = 212757
        val bankCode = "12345678"
        val customerId = "0987654321"

        val underTest = PinTanVerschluesselungskopf(partyIdentification, date, time,
            Laenderkennzeichen.Germany, bankCode, customerId)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("HNVSK:998:3+PIN:2+998+1+1::0+1:$date:$time+2:2:13:@8@        :5:1+280:$bankCode:$customerId:V:0:0+0")
    }

}