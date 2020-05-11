package net.dankito.banking.banklistcreator.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import java.io.File


@Ignore // not an automatic test, set your path to your bank list file below
class DeutscheKreditwirtschaftBankListParserTest {

    private val underTest = DeutscheKreditwirtschaftBankListParser()


    @Test
    fun parse() {

        // when
        // TODO: set path to bank list file from Deutsche Kreditwirtschaft here
        val result = underTest.parse(File(""))

        // then
        assertThat(result).hasSize(16282)

        result.forEach { bankInfo ->
            assertThat(bankInfo.name).isNotEmpty()
            assertThat(bankInfo.bankCode).isNotEmpty()
//            assertThat(bankInfo.bic).isNotEmpty() // TODO: is there a way to find BICs for all banks?
            assertThat(bankInfo.postalCode).isNotEmpty()
            assertThat(bankInfo.city).isNotEmpty()
            assertThat(bankInfo.checksumMethod).isNotEmpty()
        }
    }

}