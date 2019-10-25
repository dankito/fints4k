package net.dankito.fints.tan

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class FlickercodeDecoderTest {

    private val underTest = FlickercodeDecoder()


    @Test
    fun decodeChallenge_ExampleFromChapter_C_4_1_BCD_without_ControlByte() {

        // given
        val challenge = "070A2082901998"


        // when
        val response = underTest.decodeChallenge(challenge)


        // then
        assertThat(response.startCode).isEqualTo("2082901998")
        assertThat(response.luhnChecksum).isEqualTo(1)
        assertThat(response.xorChecksum).isEqualTo("A")
        assertThat(response.rendered).isEqualTo("070520829019981A")
    }

}