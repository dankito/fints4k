package net.dankito.banking.bankfinder

import net.dankito.utils.multiplatform.log.LoggerFactory
import net.dankito.utils.serialization.JacksonJsonSerializer


actual class BankListDeserializer {

    companion object {
        const val BankListFileName = "BankList.json"

        private val log = LoggerFactory.getLogger(InMemoryBankFinder::class)
    }


    actual fun loadBankList(): List<BankInfo> {
        try {
            val bankListString = readBankListFile()

            JacksonJsonSerializer().deserializeList(bankListString, BankInfo::class.java)?.let {
                return it
            }
        } catch (e: Exception) {
            log.error(e) { "Could not load bank list" }
        }

        return listOf()
    }

    fun readBankListFile(): String {
        val inputStream = BankFinderBase::class.java.classLoader.getResourceAsStream(BankListFileName)

        return inputStream.bufferedReader().readText()
    }

}