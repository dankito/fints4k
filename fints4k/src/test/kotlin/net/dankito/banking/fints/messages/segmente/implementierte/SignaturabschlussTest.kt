package net.dankito.banking.fints.messages.segmente.implementierte

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SignaturabschlussTest {

    @Test
    fun format() {

        // given
        val segmentNumber = 7
        val controlReference = "1902675680"
        val pin = "MyPin"

        val underTest = Signaturabschluss(segmentNumber, controlReference, pin)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("HNSHA:$segmentNumber:2+$controlReference++$pin")
    }

}