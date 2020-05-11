package net.dankito.fints.banks

import net.dankito.fints.model.BankInfo


open class InMemoryBankFinder() : BankFinderBase(), IBankFinder {

    internal constructor(bankList: List<BankInfo>) : this() {
        this.bankListField = bankList
    }


    protected var bankListField: List<BankInfo>? = null


    override fun findBankByBankCode(query: String): List<BankInfo> {
        if (query.isEmpty()) {
            return getBankList()
        }

        return getBankList().filter { it.bankCode.startsWith(query) }
    }

    override fun findBankByNameBankCodeOrCity(query: String?): List<BankInfo> {
        if (query.isNullOrEmpty()) {
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


    override fun preloadBankList() {
        findBankByBankCode("1")
    }


    override fun getBankList(): List<BankInfo> {
        bankListField?.let {
            return it
        }

        val bankList = loadBankListFile()

        this.bankListField = bankList

        return bankList
    }

}