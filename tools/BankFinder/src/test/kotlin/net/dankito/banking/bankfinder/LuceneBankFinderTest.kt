package net.dankito.banking.bankfinder

import net.dankito.utils.io.FileUtils
import org.junit.jupiter.api.AfterAll
import java.io.File


class LuceneBankFinderTest : BankFinderTestBase() {

    companion object {
        private val IndexFolder = File("testData", "index")


        @AfterAll
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