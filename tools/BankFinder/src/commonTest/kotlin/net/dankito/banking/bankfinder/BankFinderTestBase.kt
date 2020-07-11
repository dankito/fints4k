package net.dankito.banking.bankfinder

import kotlin.test.Test
import kotlin.test.assertEquals


abstract class BankFinderTestBase {

    protected abstract fun createBankFinder(): IBankFinder


    protected val underTest = createBankFinder()


    @Test
    fun findBankByBankCode_10000000() {

        // when
        val result = underTest.findBankByBankCode("10000000")

        // then
        assertEquals(1, result.size)
        assertEquals("Bundesbank", result[0].name)
    }


    @Test
    fun findBankByNameBankCodeOrCity_Starnberg_MultipleResults() {

        // when
        val result = underTest.findBankByNameBankCodeOrCity("starnberg")

        // then
        assertEquals(71, result.size)
    }

    @Test
    fun findBankByNameBankCodeOrCity_mizUh_SingleResult() {

        // when
        val result = underTest.findBankByNameBankCodeOrCity("mizUh")

        // then
        assertEquals(1, result.size)

        assertEquals("Mizuho Bank Filiale Düsseldorf", result.first().name)
    }

    @Test
    fun findBankByNameBankCodeOrCity_MultiPartsQuery_BerlinSparkas() {

        // when
        val result = underTest.findBankByNameBankCodeOrCity("Berlin Sparkas")

        // then
        assertEquals(3, result.size)

        assertEquals("Landesbank Berlin - Berliner Sparkasse", result.first().name)
    }

    @Test
    fun findBankByNameBankCodeOrCity_MultiPartsQuery_SparkasBerlin() {

        // when
        val result = underTest.findBankByNameBankCodeOrCity("Sparkas Berlin")

        // then
        assertEquals(3, result.size)

        assertEquals("Landesbank Berlin - Berliner Sparkasse", result.first().name)
    }

}