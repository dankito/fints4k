package net.dankito.banking.bankfinder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


abstract class BankFinderTestBase {

    protected abstract fun createBankFinder(): IBankFinder


    protected val underTest = createBankFinder()


    @Test
    fun findBankByBankCode_10000000() {

        // when
        val result = underTest.findBankByBankCode("10000000")

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Bundesbank")
    }


    @Test
    fun findBankByNameBankCodeOrCity_Starnberg_MultipleResults() {

        // when
        val result = underTest.findBankByNameBankCodeOrCity("starnberg")

        // then
        assertThat(result).hasSize(71)
    }

    @Test
    fun findBankByNameBankCodeOrCity_mizUh_SingleResult() {

        // when
        val result = underTest.findBankByNameBankCodeOrCity("mizUh")

        // then
        assertThat(result).hasSize(1)

        assertThat(result.first().name).isEqualTo("Mizuho Bank Filiale Düsseldorf")
    }

    @Test
    fun findBankByNameBankCodeOrCity_MultiPartsQuery_BerlinSparkas() {

        // when
        val result = underTest.findBankByNameBankCodeOrCity("Berlin Sparkas")

        // then
        assertThat(result).hasSize(3)

        assertThat(result.first().name).isEqualTo("Landesbank Berlin - Berliner Sparkasse")
    }

    @Test
    fun findBankByNameBankCodeOrCity_MultiPartsQuery_SparkasBerlin() {

        // when
        val result = underTest.findBankByNameBankCodeOrCity("Sparkas Berlin")

        // then
        assertThat(result).hasSize(3)

        assertThat(result.first().name).isEqualTo("Landesbank Berlin - Berliner Sparkasse")
    }

}