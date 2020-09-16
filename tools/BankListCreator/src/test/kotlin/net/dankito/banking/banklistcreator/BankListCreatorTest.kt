package net.dankito.banking.banklistcreator

import net.dankito.banking.banklistcreator.prettifier.BankListPrettifierOption
import org.junit.Test

import org.junit.Ignore
import java.io.File


@Ignore // not an automatic test, set your path to your bank list file in TestConfig.DeutscheKreditwirtschaftBankListXlsxFile
class BankListCreatorTest {

    private val underTest = BankListCreator()


    @Test
    fun createBankListJson() {
        // TODO: set path to bank list file from Deutsche Kreditwirtschaft in TestConfig.DeutscheKreditwirtschaftBankListXlsxFile
        underTest.createDetailedAndPrettifiedBankListFromDeutscheKreditwirtschaftXlsxFile(
            TestConfig.DeutscheKreditwirtschaftBankListXlsxFile,
            File("../BankFinder/src/commonMain/resources/DetailedBankList.json"),
            File("../BankFinder/src/commonMain/resources/BankList.json"),
            BankListPrettifierOption.values().toList()
        )
    }

}