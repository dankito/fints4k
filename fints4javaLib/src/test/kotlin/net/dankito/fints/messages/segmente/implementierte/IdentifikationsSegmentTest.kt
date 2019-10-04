package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.KundenID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.fints.messages.datenelemente.implementierte.Laenderkennzeichen
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class IdentifikationsSegmentTest {

    @Test
    fun format() {

        // given
        val underTest = IdentifikationsSegment(2, Laenderkennzeichen.Germany, "12345678", KundenID.Anonymous, KundensystemID.Anonymous, KundensystemStatusWerte.NichtBenoetigt)

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo("HKIDN:2:2+280:12345678+9999999999+0+0")
    }

}