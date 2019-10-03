package net.dankito.fints.messages.nachrichten.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.Laenderkennzeichen
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class DialoginitialisierungAnonymTest {

    @Test
    fun format() {

        // given
        val underTest = DialoginitialisierungAnonym(Laenderkennzeichen.Germany, "12345678", "36792786FA12F235F04647689", "3")

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo(
            "HNHBK:1:3+000000000125+300+0+1'" +
            "HKIDN:2:2+280:12345678+9999999999+0+0'" +
            "HKVVB:3:3+0+0+0+36792786FA12F235F04647689+3'" +
            "HNHBS:4:1+1'"
        )
    }

}