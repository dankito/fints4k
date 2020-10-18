package net.dankito.banking.banklistcreator

import net.dankito.banking.bankfinder.BankInfo
import net.dankito.banking.banklistcreator.parser.DeutscheKreditwirtschaftBankListParser
import net.dankito.banking.banklistcreator.prettifier.BankListPrettifier
import net.dankito.banking.banklistcreator.prettifier.BankListPrettifierOption
import net.dankito.utils.serialization.JacksonJsonSerializer
import java.io.File


open class BankListCreator @JvmOverloads constructor(
    protected open val parser: DeutscheKreditwirtschaftBankListParser = DeutscheKreditwirtschaftBankListParser(),
    protected open val prettifier: BankListPrettifier = BankListPrettifier()
) {

    open fun createBankListFromDeutscheKreditwirtschaftXlsxFile(deutscheKreditwirtschaftXlsxFile: File,
                                                                bankListOutputFile: File) {

        val banks = parser.parse(deutscheKreditwirtschaftXlsxFile)

        saveBankListAsJson(banks, bankListOutputFile)
    }

    open fun createDetailedAndPrettifiedBankListFromDeutscheKreditwirtschaftXlsxFile(
        deutscheKreditwirtschaftXlsxFile: File, detailedBankListOutputFile: File,
        prettifiedBankListOutputFile: File, prettifyOptions: List<BankListPrettifierOption>) {

        val allBanks = parser.parse(deutscheKreditwirtschaftXlsxFile)

        saveBankListAsJson(allBanks, detailedBankListOutputFile)

        val mappedBanks = allBanks.map { BankInfo(it.name, it.bankCode, it.bic, it.postalCode, it.city, it.pinTanAddress, it.pinTanVersion) }
        val prettifiedBanks = prettifier.prettify(mappedBanks, prettifyOptions)
        saveBankListAsJson(prettifiedBanks, prettifiedBankListOutputFile)
    }

    open fun saveBankListAsJson(banks: List<BankInfo>, bankListOutputFile: File) {
        JacksonJsonSerializer().serializeObject(banks, bankListOutputFile)
    }

}