package net.dankito.banking.fints.banks

import net.dankito.utils.io.FileUtils
import org.junit.AfterClass
import java.io.File


class LuceneBankFinderTest : BankFinderTestBase() {

    companion object {
        private val IndexFolder = File("testData", "index")


        @AfterClass
        @JvmStatic
        fun deleteIndex() {
            FileUtils().deleteFolderRecursively(IndexFolder.parentFile)
        }
    }


    override fun createBankFinder(): IBankFinder {
        return LuceneBankFinder(IndexFolder)
    }


    init {
        underTest.preloadBankList()
    }

}