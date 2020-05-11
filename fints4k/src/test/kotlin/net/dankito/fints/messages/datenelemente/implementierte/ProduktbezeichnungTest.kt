package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import org.junit.Test


class ProduktbezeichnungTest {

    @Test
    fun validate_MaxLength() {

        // given
        val underTest = Produktbezeichnung("1234567890123456789012345", Existenzstatus.Mandatory)

        // when
        underTest.validate()

        // then
        // no exception
    }

    @Test(expected = IllegalArgumentException::class)
    fun validate_MaxLengthExceeded() {

        // given
        val underTest = Produktbezeichnung("12345678901234567890123456", Existenzstatus.Mandatory)

        // when
        underTest.validate()

        // then
        // exception gets thrown
    }

}