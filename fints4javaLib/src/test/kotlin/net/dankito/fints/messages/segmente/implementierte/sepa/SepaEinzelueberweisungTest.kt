package net.dankito.fints.messages.segmente.implementierte.sepa

import net.dankito.fints.model.AccountData
import net.dankito.fints.model.BankTransferData
import net.dankito.fints.model.CustomerData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class SepaEinzelueberweisungTest {

    @Test
    fun format() {

        // given
        val segmentNumber = 7
        val debitorName = "Nelson Mandela"
        val debitorIban = "ZA123456780987654321"
        val debitorBic = "ABCDZAEFXXX"
        val creditorName = "Mahatma Gandhi"
        val creditorIban = "IN123456780987654321"
        val creditorBic = "ABCDINEFXXX"
        val amount = 1234.56.toBigDecimal()
        val usage = "What should Mahatma Gandhi want with money?"

        val underTest = SepaEinzelueberweisung(segmentNumber,
            "urn:iso:std:iso:20022:tech:xsd:pain.001.001.03",
            CustomerData("", "", "", debitorName),
            AccountData("", null, 0, "", debitorIban, "", null, null, "", null, null, listOf()),
            debitorBic,
            BankTransferData(creditorName, creditorIban, creditorBic, amount, usage)
        )


        // when
        val result = underTest.format()


        // then
        assertThat(result).contains(debitorName, debitorIban, debitorBic, creditorName, creditorIban, creditorBic,
            amount.toString(), usage, "urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.001.03")
    }
}