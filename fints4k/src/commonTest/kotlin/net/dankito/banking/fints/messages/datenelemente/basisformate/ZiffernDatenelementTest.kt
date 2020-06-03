package net.dankito.banking.fints.messages.datenelemente.basisformate

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import net.dankito.banking.fints.messages.Existenzstatus
import kotlin.test.Test


class ZiffernDatenelementTest {

    @Test
    fun format() {

        // given
        val underTest = createZiffernDatenelement(1, 6)

        // when
        val result = underTest.format()

        // then
        expect(result).toBe("000001")
    }


    @Test
    fun validate_0_Valid() {

        // given
        val underTest = createZiffernDatenelement(0, 3)

        // when
        underTest.validate()

        // then
        // no exception
    }

    @Test
    fun validate_999_Valid() {

        // given
        val underTest = createZiffernDatenelement(999, 3)

        // when
        underTest.validate()

        // then
        // no exception
    }

    @Test
    fun validate_Minus1_Invalid() {

        // given
        val underTest = createZiffernDatenelement(-1, 3)

        // when
        expect {
            underTest.validate()
        }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun validate_1000_Invalid() {

        // given
        val underTest = createZiffernDatenelement(1000, 3)

        // when
        expect {
            underTest.validate()
        }.toThrow<IllegalArgumentException>()
    }


    private fun createZiffernDatenelement(value: Int, numberOfDigits: Int): ZiffernDatenelement {
        return object : ZiffernDatenelement(value, numberOfDigits, Existenzstatus.Mandatory) { }
    }

}