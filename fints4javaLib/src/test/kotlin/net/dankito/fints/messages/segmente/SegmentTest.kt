package net.dankito.fints.messages.segmente

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.DatenelementBase
import net.dankito.fints.messages.datenelemente.basisformate.TextDatenelement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SegmentTest {

    @Test
    fun format_CutEmptyDataElementGroupsAtSegmentEnd() {

        // given
        val underTest = object : Segment(listOf(
            createTextDataElement("DE1"),
            createTextDataElement("DE2"),
            createEmptyTextDataElement(),
            createEmptyTextDataElement(),
            createEmptyTextDataElement(),
            createTextDataElement("DE6"),
            createEmptyTextDataElement(),
            createTextDataElement("DE8"),
            createEmptyTextDataElement(),
            createEmptyTextDataElement()
        ), Existenzstatus.Mandatory) { }


        // when
        val result = underTest.format()

        // then
        // assert that empty data elements at end get cut but that the empty ones in the middle remain
        assertThat(result).isEqualTo("DE1+DE2++++DE6++DE8")
    }


    private fun createEmptyTextDataElement(): DatenelementBase {
        return createTextDataElement("")
    }

    private fun createTextDataElement(text: String): DatenelementBase {
        return object : TextDatenelement(text, Existenzstatus.Optional) { }
    }

}