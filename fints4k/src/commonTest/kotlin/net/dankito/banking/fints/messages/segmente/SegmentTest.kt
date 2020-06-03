package net.dankito.banking.fints.messages.segmente

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.DatenelementBase
import net.dankito.banking.fints.messages.datenelemente.basisformate.TextDatenelement
import ch.tutteli.atrium.api.verbs.expect
import kotlin.test.Test

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
        )) { }


        // when
        val result = underTest.format()

        // then
        // assert that empty data elements at end get cut but that the empty ones in the middle remain
        expect(result).toBe("DE1+DE2++++DE6++DE8")
    }


    private fun createEmptyTextDataElement(): DatenelementBase {
        return createTextDataElement("")
    }

    private fun createTextDataElement(text: String): DatenelementBase {
        return object : TextDatenelement(text, Existenzstatus.Optional) { }
    }

}