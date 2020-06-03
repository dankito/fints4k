package net.dankito.banking.fints.messages.datenelemente.basisformate

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.fluent.en_GB.toThrow
import ch.tutteli.atrium.api.verbs.expect
import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.signatur.SicherheitsidentifikationDetails
import kotlin.test.Test


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

    @Test
    fun validate_GreekCharacter_Invalid() {

        // given
        val underTest = createTextDatenelement("α")

        // when
        expect {
            underTest.validate()
        }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun validate_CyrillicCharacter_Invalid() {

        // given
        val underTest = createTextDatenelement("Я")

        // when
        expect {
            underTest.validate()
        }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun validate_EuroSymbol_Invalid() {

        // given
        val underTest = createTextDatenelement("€") // is only allowed by ISO-8859-15, not by ISO-8859-1

        // when
        expect {
            underTest.validate()
        }.toThrow<IllegalArgumentException>()
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
        expect(result).toBe(expected)
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
        expect(result).toBe(expected)
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
        expect(result).toBe(expected)
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
        expect(result).toBe(expected)
    }


    private fun createTextDatenelement(text: String): TextDatenelement {
        return object : TextDatenelement(text, Existenzstatus.Mandatory) { }
    }

}