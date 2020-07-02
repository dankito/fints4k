package net.dankito.banking.fints.messages.segmente.implementierte.sepa

import ch.tutteli.atrium.api.fluent.en_GB.contains
import ch.tutteli.atrium.api.verbs.expect
import net.dankito.banking.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.banking.fints.model.*
import kotlin.test.Test


class SepaBankTransferBaseTest {

    companion object {

        val segmentNumber = 7
        val debitorName = "Nelson Mandela"
        val debitorIban = "ZA123456780987654321"
        val debitorBic = "ABCDZAEFXXX"
        val creditorName = "Mahatma Gandhi"
        val creditorIban = "IN123456780987654321"
        val creditorBic = "ABCDINEFXXX"
        val amount = Amount("1234,56")
        val usage = "What should Mahatma Gandhi want with money?"

    }


    @Test
    fun format_Pain001_001_03() {

        // given
        val underTest = SepaBankTransferBase(CustomerSegmentId.SepaBankTransfer, segmentNumber,
            "urn:iso:std:iso:20022:tech:xsd:pain.001.001.03",
            CustomerData("", "", "", debitorName),
            AccountData("", null, 0, "", debitorIban, "", null, null, "", null, null, listOf()),
            debitorBic,
            BankTransferData(creditorName, creditorIban, creditorBic, Money(amount, "EUR"), usage)
        )


        // when
        val result = underTest.format()


        // then
        expect(result).contains(debitorName, debitorIban, debitorBic, creditorName, creditorIban, creditorBic,
            amount.toString().replace(',', '.'), usage, "urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.001.03")
    }

    @Test
    fun format_Pain001_003_03() {

        // given
        val underTest = SepaBankTransferBase(CustomerSegmentId.SepaBankTransfer, segmentNumber,
            "urn:iso:std:iso:20022:tech:xsd:pain.001.003.03",
            CustomerData("", "", "", debitorName),
            AccountData("", null, 0, "", debitorIban, "", null, null, "", null, null, listOf()),
            debitorBic,
            BankTransferData(creditorName, creditorIban, creditorBic, Money(amount, "EUR"), usage)
        )


        // when
        val result = underTest.format()


        // then
        expect(result).contains(debitorName, debitorIban, debitorBic, creditorName, creditorIban, creditorBic,
            amount.toString().replace(',', '.'), usage, "urn?:iso?:std?:iso?:20022?:tech?:xsd?:pain.001.003.03")
    }

}