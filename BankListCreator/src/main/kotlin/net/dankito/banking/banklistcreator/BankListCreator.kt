package net.dankito.banking.banklistcreator

import net.dankito.banking.banklistcreator.parser.DeutscheKreditwirtschaftBankListParser
import net.dankito.utils.serialization.JacksonJsonSerializer
import java.io.File


open class BankListCreator @JvmOverloads constructor(
    protected val parser: DeutscheKreditwirtschaftBankListParser = DeutscheKreditwirtschaftBankListParser()
) {

    fun createBankListFromDeutscheKreditwirtschaftXlsxFile(bankFileOutputFile: File,
                                                           deutscheKreditwirtschaftXlsxFile: File) {

        val banks = parser.parse(deutscheKreditwirtschaftXlsxFile)

        JacksonJsonSerializer().serializeObject(banks, bankFileOutputFile)
    }

}