package net.dankito.fints.messages.segmente.implementierte.tan

import net.dankito.fints.FinTsTestBase
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanGeneratorTanMedium
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMediumKlasse
import net.dankito.fints.messages.datenelemente.implementierte.tan.TanMediumStatus
import net.dankito.fints.response.segments.ChangeTanMediaParameters
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TanGeneratorTanMediumAnOderUmmeldenTest: FinTsTestBase() {

    companion object {

        private const val TAN = "123456"

        private const val ATC = 12345

        private const val CardNumber = "9876543210"

        private const val FollowUpCardNumber = "02"

        private const val CardType = 11

        private const val SegmentNumber = 3

        private val NewActiveTanMedium = TanGeneratorTanMedium(TanMediumKlasse.TanGenerator, TanMediumStatus.Verfuegbar, CardNumber, FollowUpCardNumber, CardType, null, null, "EC-Card")

    }


    @Test
    fun format_Version1_AtcNotRequired_FollowUpCardNumberNotRequired() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, false, false, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(1, SegmentNumber, Bank, Customer, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo("HKTAU:$SegmentNumber:1+G+$CardNumber")
    }

    @Test
    fun format_Version1_AtcRequired_FollowUpCardNumberNotRequired() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, false, true, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(1, SegmentNumber, Bank, Customer, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo("HKTAU:$SegmentNumber:1+G+$CardNumber+++$ATC+$TAN")
    }

    @Test
    fun format_Version1_AtcNotRequired_FollowUpCardNumberRequired() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, true, false, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(1, SegmentNumber, Bank, Customer, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo("HKTAU:$SegmentNumber:1+G+$CardNumber+$FollowUpCardNumber")
    }

    @Test
    fun format_Version1_AtcRequired_FollowUpCardNumberRequired() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, true, true, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(1, SegmentNumber, Bank, Customer, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo("HKTAU:$SegmentNumber:1+G+$CardNumber+$FollowUpCardNumber++$ATC+$TAN")
    }


    @Test
    fun format_Version2_AtcNotRequired_FollowUpCardNumberNotRequired_CardTypeNotAllowed() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, false, false, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(2, SegmentNumber, Bank, Customer, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo("HKTAU:$SegmentNumber:2+G+$CardNumber+++$CustomerId::$BankCountryCode:$BankCode")
    }

    @Test
    fun format_Version2_AtcRequired_FollowUpCardNumberNotRequired_CardTypeNotAllowed() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, false, true, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(2, SegmentNumber, Bank, Customer, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo("HKTAU:$SegmentNumber:2+G+$CardNumber+++$CustomerId::$BankCountryCode:$BankCode++++$ATC+$TAN")
    }

    @Test
    fun format_Version2_AtcNotRequired_FollowUpCardNumberRequired_CardTypeNotAllowed() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, true, false, false, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(2, SegmentNumber, Bank, Customer, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo("HKTAU:$SegmentNumber:2+G+$CardNumber+$FollowUpCardNumber++$CustomerId::$BankCountryCode:$BankCode")
    }

    @Test
    fun format_Version2_AtcNotRequired_FollowUpCardNumberNotRequired_CardTypeAllowed() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, false, false, true, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(2, SegmentNumber, Bank, Customer, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo("HKTAU:$SegmentNumber:2+G+$CardNumber++$CardType+$CustomerId::$BankCountryCode:$BankCode")
    }

    @Test
    fun format_Version2_AtcRequired_FollowUpCardNumberRequired_CardTypeAllowed() {

        // given
        val parameters = ChangeTanMediaParameters(createEmptyJobParameters(), false, true, true, true, false, listOf())

        val underTest = TanGeneratorTanMediumAnOderUmmelden(2, SegmentNumber, Bank, Customer, NewActiveTanMedium, TAN, ATC, null, parameters)


        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo("HKTAU:$SegmentNumber:2+G+$CardNumber+$FollowUpCardNumber+$CardType+$CustomerId::$BankCountryCode:$BankCode++++$ATC+$TAN")
    }

    // TODO: may also test 'gueltig ab' and 'gueltig bis'


    // TODO: also test Version3

}