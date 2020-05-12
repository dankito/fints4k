package net.dankito.fints.messages.segmente.implementierte.sepa

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class SepaMessageCreatorTest {

    private val underTest = SepaMessageCreator()


    @Test
    fun containsOnlyAllowedCharacters_SimpleName() {

        // when
        val result = underTest.containsOnlyAllowedCharacters("Marieke Musterfrau")

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun containsOnlyAllowedCharacters_WithAllAllowedCharacters() {

        // when
        val result = underTest.containsOnlyAllowedCharacters("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789,.?+-/()")

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `containsOnlyAllowedCharacters colon is a legal character`() {

        // when
        val result = underTest.containsOnlyAllowedCharacters(":")

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `containsOnlyAllowedCharacters ! is an illegal character`() {

        // when
        val result = underTest.containsOnlyAllowedCharacters("!")

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `containsOnlyAllowedCharacters € is an illegal character`() {

        // when
        val result = underTest.containsOnlyAllowedCharacters("€")

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `containsOnlyAllowedCharacters @ is an illegal character`() {

        // when
        val result = underTest.containsOnlyAllowedCharacters("@")

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `containsOnlyAllowedCharacters ö is an illegal character`() {

        // when
        val result = underTest.containsOnlyAllowedCharacters("ö")

        // then
        assertThat(result).isFalse()
    }

}