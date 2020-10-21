package net.codinux.banking.tools.epcqrcode

import kotlin.test.*


class EpcQrCodeParserTest {

    private val underTest = EpcQrCodeParser()


    @Test
    fun wikipediaExampleBelgianRedCross() {

        // when
        val result = underTest.parseEpcQrCode("""
BCD
001
1
SCT
BPOTBEB1
Red Cross
BE72000000001616
EUR1
CHAR

Urgency fund
Sample EPC QR Code
""".trim())

        // then
        assertParsingSuccessful(result)

        assertEpcQrCode(result, EpcQrCodeVersion.Version1, EpcQrCodeCharacterSet.UTF_8, "SCT", "BPOTBEB1", "Red Cross",
            "BE72000000001616", "EUR", 1.0, "CHAR", null,
            "Urgency fund", "Sample EPC QR Code")
    }

    @Test
    fun spendeAnAerzteOhneGrenzen() {

        // when
        val result = underTest.parseEpcQrCode("""
BCD
001
1
SCT
BFSWDE33XXX
Ärzte ohne Grenzen e.V.
DE72370205000009709700
EUR100


Spende
Danke für Ihre Spende
""".trim())

        // then
        assertParsingSuccessful(result)

        assertEpcQrCode(result, EpcQrCodeVersion.Version1, EpcQrCodeCharacterSet.UTF_8, "SCT", "BFSWDE33XXX", "Ärzte ohne Grenzen e.V.",
            "DE72370205000009709700", "EUR", 100.00, null, null,
            "Spende", "Danke für Ihre Spende")
    }

    @Test
    fun stuzzaExample01() {

        // when
        val result = underTest.parseEpcQrCode("""
BCD
001
1
SCT
BICVXXDD123
35 Zeichen langer Empfängername zum
XX17LandMitLangerIBAN2345678901234
EUR12345689.01

35ZeichenLangeREFzurZuordnungBeimBe

Netter Text für den Zahlenden, damit dieser weiß, was er zahlt und auc
""".trim())

        // then
        assertParsingSuccessful(result)

        assertEpcQrCode(result, EpcQrCodeVersion.Version1, EpcQrCodeCharacterSet.UTF_8, "SCT", "BICVXXDD123", "35 Zeichen langer Empfängername zum",
            "XX17LandMitLangerIBAN2345678901234", "EUR", 12345689.01, null, "35ZeichenLangeREFzurZuordnungBeimBe",
            null, "Netter Text für den Zahlenden, damit dieser weiß, was er zahlt und auc")
    }

    @Test
    fun stuzzaExample02() {

        // when
        val result = underTest.parseEpcQrCode("""
BCD
001
1
SCT
GIBAATWW
Max Mustermann
AT682011131032423628
EUR1456.89

457845789452

Diverse Autoteile, Re 789452 KN 457845
""".trim())

        // then
        assertParsingSuccessful(result)

        assertEpcQrCode(result, EpcQrCodeVersion.Version1, EpcQrCodeCharacterSet.UTF_8, "SCT", "GIBAATWW", "Max Mustermann",
            "AT682011131032423628", "EUR", 1456.89, null, "457845789452",
            null, "Diverse Autoteile, Re 789452 KN 457845")
    }

    @Test
    fun stuzzaExample07() {

        // when
        val result = underTest.parseEpcQrCode("""
BCD
002
2
SCT

35 Zeichen langer Empfängername zum
XX17LandMitLangerIBAN2345678901234
EUR12345689.01

35ZeichenLangeREFzurZuordnungBeimBe

Netter Text für den Zahlenden, damit dieser weiß, was er zahlt und auc
""".trim())

        // then
        assertParsingSuccessful(result)

        assertEpcQrCode(result, EpcQrCodeVersion.Version2, EpcQrCodeCharacterSet.ISO_8895_1, "SCT", null, "35 Zeichen langer Empfängername zum",
            "XX17LandMitLangerIBAN2345678901234", "EUR", 12345689.01, null, "35ZeichenLangeREFzurZuordnungBeimBe",
            null, "Netter Text für den Zahlenden, damit dieser weiß, was er zahlt und auc")
    }


    private fun assertParsingSuccessful(result: ParseEpcQrCodeResult) {
        assertTrue(result.successful)
        assertNull(result.error)
        assertNotNull(result.epcQrCode)
    }

    private fun assertEpcQrCode(result: ParseEpcQrCodeResult, version: EpcQrCodeVersion, coding: EpcQrCodeCharacterSet, function: String,
                                bic: String?, receiver: String, iban: String, currency: String?, amount: Double?,
                                purposeCode: String?, reference: String?, text: String?, displayText: String?) {

        result.epcQrCode?.let { epcQrCode ->
            assertEquals(version, epcQrCode.version)
            assertEquals(coding, epcQrCode.coding)
            assertEquals(function, epcQrCode.function)
            assertEquals(bic, epcQrCode.bic)
            assertEquals(receiver, epcQrCode.receiverName)
            assertEquals(iban, epcQrCode.iban)
            assertEquals(currency, epcQrCode.currencyCode)
            assertEquals(amount, epcQrCode.amount)
            assertEquals(purposeCode, epcQrCode.purposeCode)
            assertEquals(reference, epcQrCode.remittanceReference)
            assertEquals(text, epcQrCode.remittanceText)
            assertEquals(displayText, epcQrCode.originatorInformation)
        }
    }

}