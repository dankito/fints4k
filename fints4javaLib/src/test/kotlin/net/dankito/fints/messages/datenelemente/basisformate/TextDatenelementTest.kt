package net.dankito.fints.messages.datenelemente.basisformate

import net.dankito.fints.messages.Existenzstatus
import org.junit.Test


class TextDatenelementTest {

    @Test
    fun validate_ValidCharacters() {

        // given
        val underTest = createTextDatenelement("àéôçñøäöüß¿")

        // when
        underTest.validate()

        // then
        // no exception
    }

    @Test(expected = IllegalArgumentException::class)
    fun validate_GreekCharacter_Invalid() {

        // given
        val underTest = createTextDatenelement("α")

        // when
        underTest.validate()

        // then
        // exception gets thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun validate_CyrillicCharacter_Invalid() {

        // given
        val underTest = createTextDatenelement("Я")

        // when
        underTest.validate()

        // then
        // exception gets thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun validate_EuroSymbol_Invalid() {

        // given
        val underTest = createTextDatenelement("€") // is only allowed by ISO-8859-15, not by ISO-8859-1

        // when
        underTest.validate()

        // then
        // exception gets thrown
    }


    private fun createTextDatenelement(text: String): TextDatenelement {
        return object : TextDatenelement(text, Existenzstatus.Mandatory) { }
    }

}