package net.dankito.banking.fints.util

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import net.dankito.utils.multiplatform.Date
import kotlin.test.Test

class FinTsUtilsTest {

    private val underTest = FinTsUtils()


    @Test
    fun formatDate() {

        // given
        val date = Date(1988, 3, 27)

        // when
        val result = underTest.formatDate(date)

        // then
        expect(result).toBe("19880327")
    }

    @Test
    fun formatDateAsInt() {

        // given
        val date = Date(1988, 3, 27)

        // when
        val result = underTest.formatDateAsInt(date)

        // then
        expect(result).toBe(19880327)
    }


    @Test
    fun formatTime_AM() {

        // given
        val time = Date(8, 2, 1)

        // when
        val result = underTest.formatTime(time)

        // then
        expect(result).toBe("080201")
    }

    @Test
    fun formatTime_PM() {

        // given
        val time = Date(18, 22, 51)

        // when
        val result = underTest.formatTime(time)

        // then
        expect(result).toBe("182251")
    }

    @Test
    fun formatTimeAsInt_AM() {

        // given
        val time = Date(8, 2, 1)

        // when
        val result = underTest.formatTimeAsInt(time)

        // then
        expect(result).toBe(80201)
    }

    @Test
    fun formatTimeAsInt_PM() {

        // given
        val time = Date(18, 22, 51)

        // when
        val result = underTest.formatTimeAsInt(time)

        // then
        expect(result).toBe(182251)
    }

}