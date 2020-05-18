package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class BPDVersionTest {

    @Test
    fun format() {

        // given
        val underTest = BPDVersion(3, Existenzstatus.Mandatory)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("3")
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

    @Test(expected = IllegalArgumentException::class)
    fun validate_TooLow() {

        // given
        val underTest = BPDVersion(-1, Existenzstatus.Mandatory)

        // when
        underTest.validate()

        // then
        // exception gets thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun validate_TooHigh() {

        // given
        val underTest = BPDVersion(1000, Existenzstatus.Mandatory)

        // when
        underTest.validate()

        // then
        // exception gets thrown
    }

}