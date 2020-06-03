package net.dankito.banking.fints.messages.segmente.implementierte.sepa

import ch.tutteli.atrium.api.verbs.expect
import net.dankito.banking.fints.extensions.isFalse
import net.dankito.banking.fints.extensions.isTrue
import kotlin.test.Test


class SepaMessageCreatorTest {

    private val underTest = SepaMessageCreator()


    @Test
    fun containsOnlyAllowedCharacters_SimpleName() {

        // when
        val result = underTest.containsOnlyAllowedCharacters("Marieke Musterfrau")

        // then
        expect(result).isTrue()
    }

    @Test
    fun containsOnlyAllowedCharacters_WithAllAllowedCharacters() {

        // when
        val result = underTest.containsOnlyAllowedCharacters("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789,.?+-/()")

        // then
        expect(result).isTrue()
    }

    @Test
    fun `containsOnlyAllowedCharacters colon is a legal character`() {

        // when
        val result = underTest.containsOnlyAllowedCharacters(":")

        // then
        expect(result).isTrue()
    }

    @Test
    fun `containsOnlyAllowedCharacters ! is an illegal character`() {

        // when
        val result = underTest.containsOnlyAllowedCharacters("!")

        // then
        expect(result).isFalse()
    }

    @Test
    fun `containsOnlyAllowedCharacters € is an illegal character`() {

        // when
        val result = underTest.containsOnlyAllowedCharacters("€")

        // then
        expect(result).isFalse()
    }

    @Test
    fun `containsOnlyAllowedCharacters @ is an illegal character`() {

        // when
        val result = underTest.containsOnlyAllowedCharacters("@")

        // then
        expect(result).isFalse()
    }

    @Test
    fun `containsOnlyAllowedCharacters ö is an illegal character`() {

        // when
        val result = underTest.containsOnlyAllowedCharacters("ö")

        // then
        expect(result).isFalse()
    }

}