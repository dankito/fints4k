package net.dankito.fints.tan

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class FlickercodeDecoderTest {

    private val underTest = FlickercodeDecoder()


    @Test
    fun decodeChallenge_ExampleFromChapter_C_4_1_BCD_without_ControlByte_and_DataElements() {

        // given
        val challenge = "070A2082901998"


        // when
        val response = underTest.decodeChallenge(challenge)


        // then
        assertThat(response.parsedDataSet).isEqualTo("070520829019981A")
    }


    @Test
    fun decodeChallenge_ExampleFromChapter_C_4_1_without_ControlByte() {

        // given
        val challenge = "070A208290199872IE99BOFI"


        // when
        val response = underTest.decodeChallenge(challenge)


        // then
        assertThat(response.parsedDataSet).isEqualTo("100520829019981849453939424F46494B")
    }

    @Test
    fun decodeChallenge_AmountInASCII() {

        // given
        val challenge = "2908881696281098765432100532,00"

        // when
        val result = underTest.decodeChallenge(challenge)

        // then
        assertThat(result.parsedDataSet).isEqualTo("1204881696280598765432101533322C30303A")
    }

    @Test
    fun decodeChallenge_CheckLuhnSumModulo10Is0() {

        // given
        val challenge = "100880040243"

        // when
        val result = underTest.decodeChallenge(challenge)

        // then
        assertThat(result.parsedDataSet).isEqualTo("0604800402430B")
    }

}