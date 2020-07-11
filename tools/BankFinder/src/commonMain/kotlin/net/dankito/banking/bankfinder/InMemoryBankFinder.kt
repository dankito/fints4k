package net.dankito.banking.bankfinder


open class InMemoryBankFinder() : BankFinderBase(), IBankFinder {

    constructor(bankList: List<BankInfo>) : this() {
        this.bankListField = bankList
    }


    protected var bankListField: List<BankInfo>? = null


    override fun findBankByBankCode(query: String): List<BankInfo> {
        if (query.isEmpty()) {
            return getBankList()
        }

        return getBankList().filter { it.bankCode.startsWith(query) }
    }

    override fun findBankByNameOrCityForNonEmptyQuery(query: String): List<BankInfo> {
        val queryPartsLowerCase = query.toLowerCase().split(" ", "-")

        return getBankList().filter { bankInfo ->
            checkIfAllQueryPartsMatchBankNameOrCity(queryPartsLowerCase, bankInfo)
        }
    }

    protected open fun checkIfAllQueryPartsMatchBankNameOrCity(queryPartsLowerCase: List<String>, bankInfo: BankInfo): Boolean {
        for (queryPartLowerCase in queryPartsLowerCase) {
            if (checkIfQueryMatchesBankNameOrCity(bankInfo, queryPartLowerCase) == false) {
                return false
            }
        }

        return true
    }

    protected open fun checkIfQueryMatchesBankNameOrCity(bankInfo: BankInfo, queryLowerCase: String): Boolean {
        return bankInfo.name.toLowerCase().contains(queryLowerCase)
                || bankInfo.city.toLowerCase().contains(queryLowerCase)
    }


    override fun searchBankByBic(bic: String): BankInfo? {
        return getBankList().firstOrNull { it.bic == bic }
    }


    override fun preloadBankList() {
        findBankByBankCode("1")
    }


    override fun getBankList(): List<BankInfo> {
        bankListField?.let {
            return it
        }

        val bankList = loadBankList()

        this.bankListField = bankList

        return bankList
    }

}