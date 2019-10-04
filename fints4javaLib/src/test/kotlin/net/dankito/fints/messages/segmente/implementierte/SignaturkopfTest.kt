package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.Laenderkennzeichen
import net.dankito.fints.messages.datenelemente.implementierte.signatur.IdentifizierungDerPartei
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsfunktion
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class SignaturkopfTest {

    @Test
    fun format() {

        // given
        val securityFunction = Sicherheitsfunktion.PIN_TAN_911
        val controlReference = "1902675680"
        val partyIdentification = IdentifizierungDerPartei.SynchronizingCustomerSystemId
        val date = 20191002
        val time = 212757
        val bankCode = "12345678"
        val customerId = "0987654321"
        val keyNumber = 0
        val keyVersion = 0

        val underTest = PinTanSignaturkopf(2, securityFunction, controlReference, partyIdentification,
            date, time, Laenderkennzeichen.Germany, bankCode, customerId)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("HNSHK:2:4+PIN:2+${securityFunction.code}+$controlReference+1+1+1::0+1+1:$date:$time+1:999:1+6:10:16+280:$bankCode:$customerId:S:$keyNumber:$keyVersion")
    }

}