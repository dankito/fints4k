package net.dankito.banking.fints.messages.segmente.implementierte.tan

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import net.dankito.banking.fints.FinTsTestBase
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMediumKlasse
import net.dankito.banking.fints.messages.datenelemente.implementierte.tan.TanMediumStatus
import net.dankito.banking.fints.response.segments.ChangeTanMediaParameters
import kotlin.test.Test


class TanGeneratorTanMediumAnOderUmmeldenTest: FinTsTestBase() {

    companion object {

        private const val TAN = "123456"

        private const val ATC = 12345

        private const val CardNumber = "9876543210"

        private const val CardSequenceNumber = "02"

        private const val CardType = 11

        private const val SegmentNumber = 3

        private val NewActiveTanMedium = TanGeneratorTanMedium(TanMediumKlasse.TanGenerator, TanMediumStatus.Verfuegbar, CardNumber, CardSequenceNumber, CardType, null, null, "EC-Card")

    }


    init {
        Bank.addAccount(Account)
    }


    @Test
    fun format_Version1_AtcNotRequired_CardSequenceNumberNotRequired() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, false, false, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(1, SegmentNumber, Bank, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        expect(result).toBe("HKTAU:$SegmentNumber:1+G+$CardNumber")
    }

    @Test
    fun format_Version1_AtcRequired_CardSequenceNumberNotRequired() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, false, true, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(1, SegmentNumber, Bank, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        expect(result).toBe("HKTAU:$SegmentNumber:1+G+$CardNumber+++$ATC+$TAN")
    }

    @Test
    fun format_Version1_AtcNotRequired_CardSequenceNumberRequired() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, true, false, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(1, SegmentNumber, Bank, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        expect(result).toBe("HKTAU:$SegmentNumber:1+G+$CardNumber+$CardSequenceNumber")
    }

    @Test
    fun format_Version1_AtcRequired_CardSequenceNumberRequired() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, true, true, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(1, SegmentNumber, Bank, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        expect(result).toBe("HKTAU:$SegmentNumber:1+G+$CardNumber+$CardSequenceNumber++$ATC+$TAN")
    }


    @Test
    fun format_Version2_AtcNotRequired_CardSequenceNumberNotRequired_CardTypeNotAllowed() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, false, false, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(2, SegmentNumber, Bank, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        expect(result).toBe("HKTAU:$SegmentNumber:2+G+$CardNumber+++$CustomerId::$BankCountryCode:$BankCode")
    }

    @Test
    fun format_Version2_AtcRequired_CardSequenceNumberNotRequired_CardTypeNotAllowed() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, false, true, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(2, SegmentNumber, Bank, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        expect(result).toBe("HKTAU:$SegmentNumber:2+G+$CardNumber+++$CustomerId::$BankCountryCode:$BankCode++++$ATC+$TAN")
    }

    @Test
    fun format_Version2_AtcNotRequired_CardSequenceNumberRequired_CardTypeNotAllowed() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, true, false, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(2, SegmentNumber, Bank, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        expect(result).toBe("HKTAU:$SegmentNumber:2+G+$CardNumber+$CardSequenceNumber++$CustomerId::$BankCountryCode:$BankCode")
    }

    @Test
    fun format_Version2_AtcNotRequired_CardSequenceNumberNotRequired_CardTypeAllowed() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, false, false, true, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(2, SegmentNumber, Bank, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        expect(result).toBe("HKTAU:$SegmentNumber:2+G+$CardNumber++$CardType+$CustomerId::$BankCountryCode:$BankCode")
    }

    @Test
    fun format_Version2_AtcRequired_CardSequenceNumberRequired_CardTypeAllowed() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, true, true, true, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(2, SegmentNumber, Bank, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        expect(result).toBe("HKTAU:$SegmentNumber:2+G+$CardNumber+$CardSequenceNumber+$CardType+$CustomerId::$BankCountryCode:$BankCode++++$ATC+$TAN")
    }

    // TODO: may also test 'gueltig ab' and 'gueltig bis'


    // TODO: also test Version3

}