package net.dankito.fints.banks

import net.dankito.fints.model.BankInfo
import net.dankito.utils.serialization.JacksonJsonSerializer
import org.slf4j.LoggerFactory


open class BankFinder {

    companion object {
        private val log = LoggerFactory.getLogger(BankFinder::class.java)
    }


    protected var bankListField: List<BankInfo>? = null


    open fun findBankByBankCode(query: String): List<BankInfo> {
        return getBankList().filter { it.bankCode.startsWith(query) }
    }

    open fun findBankByNameBankCodeOrCity(query: String): List<BankInfo> {
        val queryLowerCase = query.toLowerCase()

        return getBankList().filter {
            it.name.toLowerCase().contains(queryLowerCase)
                    || it.bankCode.startsWith(query)
                    || it.city.toLowerCase().contains(queryLowerCase)
        }
    }


    open fun getBankList(): List<BankInfo> {
        bankListField?.let {
            return it
        }

        val bankList = loadBankList()

        this.bankListField = bankList

        return bankList
    }

    protected open fun loadBankList(): List<BankInfo> {
        try {
            val inputStream = BankFinder::class.java.classLoader.getResourceAsStream("BankList.json")

            val bankListString = inputStream.bufferedReader().readText()

            JacksonJsonSerializer().deserializeList(bankListString, BankInfo::class.java)?.let {
                return it
            }
        } catch (e: Exception) {
            log.error("Could not load bank list", e)
        }

        return listOf()
    }

}