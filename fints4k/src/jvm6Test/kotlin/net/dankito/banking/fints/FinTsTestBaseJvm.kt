package net.dankito.banking.fints

import java.io.File
import java.nio.charset.Charset


open class FinTsTestBaseJvm : FinTsTestBase() {

    companion object {
        const val TestFilesFolderName = "test_files/"

        const val TransactionsMt940Filename = "TransactionsMt940.txt"
    }


    /**
     * testFilename has to be a file in src/test/resources/test_files/ folder
     */
    protected open fun loadTestFile(testFilename: String, charset: Charset = Charsets.UTF_8): String {
        val fileStream = FinTsTestBaseJvm::class.java.classLoader.getResourceAsStream(File(TestFilesFolderName, testFilename).path)

        return fileStream.reader(charset).readText()
    }

}