package net.dankito.banking.fints.messages.datenelemente.implementierte

import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import net.dankito.banking.fints.messages.Existenzstatus
import kotlin.test.Test


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

    @Test
    fun validate_MaxLengthExceeded() {

        // given
        val underTest = Produktbezeichnung("12345678901234567890123456", Existenzstatus.Mandatory)

        // when
        expect {
            underTest.validate()
        }.toThrow<IllegalArgumentException>()
    }

}