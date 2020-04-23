package net.dankito.banking.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class InputValidatorTest {

    private val underTest = InputValidator()


    @Test
    fun getInvalidIbanCharacters() {

        // given
        val invalidIbanCharacters = "ajvz!@#$%^&*()-_=+[]{}'\"\\|/?.,;:<>"

        // when
        val result = underTest.getInvalidIbanCharacters("EN${invalidIbanCharacters}1234")

        // then
        assertThat(result).isEqualTo(invalidIbanCharacters as Any)
    }

    @Test
    fun getInvalidSepaCharacters() {

        // given
        val invalidSepaCharacters = "!€@#$%^&*_=[]{}\\|;<>"

        // when
        val result = underTest.getInvalidSepaCharacters("abcd${invalidSepaCharacters}1234")

        // then
        assertThat(result).isEqualTo(invalidSepaCharacters as Any)
    }

}