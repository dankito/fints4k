package net.dankito.fints.banks

import net.dankito.fints.model.BankInfo
import net.dankito.utils.serialization.JacksonJsonSerializer
import org.slf4j.LoggerFactory


abstract class BankFinderBase : IBankFinder {

    companion object {
        const val BankListFileName = "BankList.json"

        private val log = LoggerFactory.getLogger(InMemoryBankFinder::class.java)
    }


    protected open fun loadBankListFile(): List<BankInfo> {
        try {
            val bankListString = readBankListFile()

            JacksonJsonSerializer().deserializeList(bankListString, BankInfo::class.java)?.let {
                return it
            }
        } catch (e: Exception) {
            log.error("Could not load bank list", e)
        }

        return listOf()
    }

    protected open fun readBankListFile(): String {
        val inputStream = BankFinderBase::class.java.classLoader.getResourceAsStream(BankListFileName)

        return inputStream.bufferedReader().readText()
    }

}