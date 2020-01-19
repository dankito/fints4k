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
        if (query.isEmpty()) {
            return getBankList()
        }

        return getBankList().filter { it.bankCode.startsWith(query) }
    }

    open fun findBankByNameBankCodeOrCity(query: String): List<BankInfo> {
        if (query.isEmpty()) {
            return getBankList()
        }

        val queryLowerCase = query.toLowerCase()
        val queryPartsLowerCase = queryLowerCase.split(" ")

        return getBankList().filter { bankInfo ->
            checkIfAllQueryPartsMatchBank(queryPartsLowerCase, bankInfo)
        }
    }

    protected open fun checkIfAllQueryPartsMatchBank(queryPartsLowerCase: List<String>, bankInfo: BankInfo): Boolean {
        for (queryPartLowerCase in queryPartsLowerCase) {
            if (checkIfQueryMatchesBank(bankInfo, queryPartLowerCase) == false) {
                return false
            }
        }

        return true
    }

    protected open fun checkIfQueryMatchesBank(bankInfo: BankInfo, queryLowerCase: String): Boolean {
        return bankInfo.name.toLowerCase().contains(queryLowerCase)
                || bankInfo.bankCode.startsWith(queryLowerCase)
                || bankInfo.city.toLowerCase().contains(queryLowerCase)
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