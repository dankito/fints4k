package net.dankito.banking.fints.messages.datenelemente.basisformate

import net.dankito.banking.fints.messages.Existenzstatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ZiffernDatenelementTest {

    @Test
    fun format() {

        // given
        val underTest = createZiffernDatenelement(1, 6)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("000001")
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

    @Test(expected = IllegalArgumentException::class)
    fun validate_Minus1_Invalid() {

        // given
        val underTest = createZiffernDatenelement(-1, 3)

        // when
        underTest.validate()

        // then
        // exception gets thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun validate_1000_Invalid() {

        // given
        val underTest = createZiffernDatenelement(1000, 3)

        // when
        underTest.validate()

        // then
        // exception gets thrown
    }


    private fun createZiffernDatenelement(value: Int, numberOfDigits: Int): ZiffernDatenelement {
        return object : ZiffernDatenelement(value, numberOfDigits, Existenzstatus.Mandatory) { }
    }

}