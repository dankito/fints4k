package net.dankito.banking.fints.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class FinTsUtilsTest {

    private val underTest = FinTsUtils()


    @Test
    fun formatDate() {

        // given
        val date = Date(88, 2, 27)

        // when
        val result = underTest.formatDate(date)

        // then
        assertThat(result).isEqualTo("19880327")
    }

    @Test
    fun formatDateAsInt() {

        // given
        val date = Date(88, 2, 27)

        // when
        val result = underTest.formatDateAsInt(date)

        // then
        assertThat(result).isEqualTo(19880327)
    }


    @Test
    fun formatTime_AM() {

        // given
        val date = Date(119, 9, 1, 8, 2, 1)

        // when
        val result = underTest.formatTime(date)

        // then
        assertThat(result).isEqualTo("080201")
    }

    @Test
    fun formatTime_PM() {

        // given
        val date = Date(119, 9, 1, 18, 22, 51)

        // when
        val result = underTest.formatTime(date)

        // then
        assertThat(result).isEqualTo("182251")
    }

    @Test
    fun formatTimeAsInt_AM() {

        // given
        val date = Date(119, 9, 1, 8, 2, 1)

        // when
        val result = underTest.formatTimeAsInt(date)

        // then
        assertThat(result).isEqualTo(80201)
    }

    @Test
    fun formatTimeAsInt_PM() {

        // given
        val date = Date(119, 9, 1, 18, 22, 51)

        // when
        val result = underTest.formatTimeAsInt(date)

        // then
        assertThat(result).isEqualTo(182251)
    }

}