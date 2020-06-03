package net.dankito.banking.fints.messages.datenelemente.implementierte

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import net.dankito.banking.fints.messages.Existenzstatus
import kotlin.test.Test


class BPDVersionTest {

    @Test
    fun format() {

        // given
        val underTest = BPDVersion(3, Existenzstatus.Mandatory)

        // when
        val result = underTest.format()

        // then
        expect(result).toBe("3")
    }


    @Test
    fun validate_0_Valid() {

        // given
        val underTest = BPDVersion(0, Existenzstatus.Mandatory)

        // when
        underTest.validate()

        // then
        // no exception
    }

    @Test
    fun validate_999_Valid() {

        // given
        val underTest = BPDVersion(999, Existenzstatus.Mandatory)

        // when
        underTest.validate()

        // then
        // no exception
    }

    @Test
    fun validate_TooLow() {

        // given
        val underTest = BPDVersion(-1, Existenzstatus.Mandatory)

        // when
        expect {
            underTest.validate()
        }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun validate_TooHigh() {

        // given
        val underTest = BPDVersion(1000, Existenzstatus.Mandatory)

        // when
        expect {
            underTest.validate()
        }.toThrow<IllegalArgumentException>()
    }

}