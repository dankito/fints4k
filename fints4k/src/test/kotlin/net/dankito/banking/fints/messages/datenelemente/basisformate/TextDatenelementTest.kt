package net.dankito.banking.fints.messages.datenelemente.basisformate

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.signatur.SicherheitsidentifikationDetails
import org.assertj.core.api.Assertions.assertThat
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


    @Test
    fun maskDataElementsSeparator() {

        // given
        val customerSystemIdWithDataElementsSeparator = "f8Clj:x3BG4BAACI/459llkXrAQA"

        val expected = "1::" + customerSystemIdWithDataElementsSeparator.replace(":", "?:")

        val underTest = SicherheitsidentifikationDetails(customerSystemIdWithDataElementsSeparator)

        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun maskDataElementGroupsSeparator() {

        // given
        val customerSystemIdWithDataElementGroupsSeparator = "f8Clj+x3BG4BAACI/459llkXrAQA"

        val expected = "1::" + customerSystemIdWithDataElementGroupsSeparator.replace("+", "?+")

        val underTest = SicherheitsidentifikationDetails(customerSystemIdWithDataElementGroupsSeparator)

        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun maskSegmentsSeparator() {

        // given
        val customerSystemIdWithSegmentsSeparator = "f8Clj\'x3BG4BAACI/459llkXrAQA"

        val expected = "1::" + customerSystemIdWithSegmentsSeparator.replace("'", "?'")

        val underTest = SicherheitsidentifikationDetails(customerSystemIdWithSegmentsSeparator)

        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun maskMaskingCharacter() {

        // given
        val customerSystemIdWithMaskingCharacter = "f8Clj?x3BG4BAACI/459llkXrAQA"

        val expected = "1::" + customerSystemIdWithMaskingCharacter.replace("?", "??")

        val underTest = SicherheitsidentifikationDetails(customerSystemIdWithMaskingCharacter)


        // when
        val result = underTest.format()


        // then
        assertThat(result).isEqualTo(expected)
    }


    private fun createTextDatenelement(text: String): TextDatenelement {
        return object : TextDatenelement(text, Existenzstatus.Mandatory) { }
    }

}