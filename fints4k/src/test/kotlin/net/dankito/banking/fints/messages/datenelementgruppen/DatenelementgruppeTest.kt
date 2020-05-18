package net.dankito.banking.fints.messages.datenelementgruppen

import net.dankito.banking.fints.messages.Separators
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.signatur.BenutzerdefinierteSignatur
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class DatenelementgruppeTest {

    @Test
    fun format_LastDataElementIsSet_GetsWrittenToOutput() {

        // given
        val pin = "12345"
        val tan = "654321"
        val dataElementGroup = BenutzerdefinierteSignatur(pin, tan)

        // when
        val result = dataElementGroup.format()

        // then
        assertThat(result).isEqualTo(pin + Separators.DataElementsSeparator + tan) // ":" does not get written to output
    }

    @Test
    fun format_LastDataElementIsEmpty_EmptyLastDataElementDoesNotGetWrittenToOutput() {

        // given
        val pin = "12345"
        val tan = null
        val dataElementGroup = BenutzerdefinierteSignatur(pin, tan)

        // when
        val result = dataElementGroup.format()

        // then
        assertThat(result).isEqualTo(pin) // ":" does not get written to output
    }

    @Test
    fun format_AllDataElementsAreEmpty_NothingGetsWrittenToOutput() {

        // given
        val pin = ""
        val tan = null
        val dataElementGroup = BenutzerdefinierteSignatur(pin, tan)

        // when
        val result = dataElementGroup.format()

        // then
        assertThat(result).isEmpty() // ":" does not get written to output
    }

}