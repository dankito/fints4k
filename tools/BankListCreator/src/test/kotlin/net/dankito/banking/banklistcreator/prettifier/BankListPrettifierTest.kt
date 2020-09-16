package net.dankito.banking.banklistcreator.prettifier

import net.dankito.banking.banklistcreator.TestConfig
import net.dankito.banking.banklistcreator.parser.DeutscheKreditwirtschaftBankListParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test


@Ignore // not an automatic test, set your path to your bank list file in TestConfig.DeutscheKreditwirtschaftBankListXlsxFile
class BankListPrettifierTest {

    private val underTest = BankListPrettifier()

    private val allBanks = DeutscheKreditwirtschaftBankListParser().parse(TestConfig.DeutscheKreditwirtschaftBankListXlsxFile)


    @Test
    fun mapBankNamesToWellKnownNames() {

        // when
        val result = underTest.mapBankNamesToWellKnownNames(allBanks)

        // then
        assertThat(result).hasSize(allBanks.size)

        val resultingBanksNames = result.map { it.name }

        assertThat(resultingBanksNames.filter { it.contains("DB Privat- und Firmenkundenbank") }).isEmpty()
    }

    @Test
    fun removeBanksWithSameBankCodeAndPostalCode() {

        // when
        val result = underTest.removeBanksWithSameBankCodeAndPostalCode(allBanks)

        // then
        assertThat(result).hasSizeLessThan(allBanks.size)

        val resultingBanksNames = result.map { it.name }

        assertThat(resultingBanksNames.filter { it.contains("PSD Bank München (Gf P2)") }).isEmpty()
    }

}