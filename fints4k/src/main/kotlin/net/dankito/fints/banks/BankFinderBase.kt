package net.dankito.fints.banks

import net.dankito.fints.model.BankInfo
import net.dankito.utils.serialization.JacksonJsonSerializer
import org.slf4j.LoggerFactory


abstract class BankFinderBase : IBankFinder {

    companion object {
        const val BankListFileName = "BankList.json"

        private val log = LoggerFactory.getLogger(InMemoryBankFinder::class.java)
    }


    protected abstract fun findBankByNameOrCityForNonEmptyQuery(query: String): List<BankInfo>


    override fun findBankByNameBankCodeOrCity(query: String?): List<BankInfo> {
        if (query.isNullOrBlank()) {
            return getBankList()
        }

        if (query.toIntOrNull() != null) { // if query is an integer, then it can only be an bank code, but not a bank name or city
            return findBankByBankCode(query)
        }

        return findBankByNameOrCityForNonEmptyQuery(query)
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