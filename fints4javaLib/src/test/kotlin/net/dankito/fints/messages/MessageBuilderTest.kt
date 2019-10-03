package net.dankito.fints.messages

import net.dankito.fints.messages.datenelemente.implementierte.Laenderkennzeichen
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class MessageBuilderTest {

    private val underTest = MessageBuilder()


    @Test
    fun createAnonymousDialogInitMessage() {

        // given
        val underTest = underTest.createAnonymousDialogInitMessage(
            Laenderkennzeichen.Germany, "12345678", "FinTS-TestClient25Stellen", "1")

        // when
        val result = underTest.format()

        // then
        assertThat(result).isEqualTo(
            "HNHBK:1:3+000000000125+300+0+1'" +
            "HKIDN:2:2+280:12345678+9999999999+0+0'" +
            "HKVVB:3:3+0+0+0+FinTS-TestClient25Stellen+1'" +
            "HNHBS:4:1+1'"
        )
    }

}