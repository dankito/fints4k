package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.FinTsTestBase
import net.dankito.fints.model.MessageBaseData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class IdentifikationsSegmentTest : FinTsTestBase() {

    @Test
    fun format() {

        // given
        val underTest = IdentifikationsSegment(2, MessageBaseData(Bank, Customer, Product))

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("HKIDN:2:2+280:12345678+0987654321+0+0")
    }

}