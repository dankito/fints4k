package net.dankito.banking.fints.messages.segmente.implementierte

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import net.dankito.banking.fints.FinTsTestBase
import net.dankito.banking.fints.model.MessageBaseData
import ch.tutteli.atrium.api.verbs.expect
import kotlin.test.Test


class IdentifikationsSegmentTest : FinTsTestBase() {

    @Test
    fun format() {

        // given
        val underTest = IdentifikationsSegment(2, MessageBaseData(Bank, Product))

        // when
        val result = underTest.format()

        // then
        expect(result).toBe("HKIDN:2:2+280:12345678+0987654321+0+0")
    }

}