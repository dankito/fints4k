package net.dankito.fints.banks

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BankFinderTest {

    private val underTest = BankFinder()


    @Test
    fun findBankByBankCode_10000000() {

        // when
        val result = underTest.findBankByBankCode("10000000")

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Bundesbank")
    }

    @Test
    fun findBankByNameBankCodeOrCity_starnberg() {

        // when
        val result = underTest.findBankByNameBankCodeOrCity("starnberg")

        // then
        assertThat(result).hasSize(66)
    }

    @Test
    fun findBankByNameBankCodeOrCity_mizUh() {

        // when
        val result = underTest.findBankByNameBankCodeOrCity("mizUh")

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Mizuho Bank Filiale Düsseldorf")
    }

}