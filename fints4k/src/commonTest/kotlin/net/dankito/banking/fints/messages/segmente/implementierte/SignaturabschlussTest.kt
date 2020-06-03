package net.dankito.banking.fints.messages.segmente.implementierte

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import kotlin.test.Test

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
        expect(result).toBe("HNSHA:$segmentNumber:2+$controlReference++$pin")
    }

}