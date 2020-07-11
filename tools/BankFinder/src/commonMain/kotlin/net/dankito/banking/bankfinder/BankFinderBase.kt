package net.dankito.banking.bankfinder


abstract class BankFinderBase : IBankFinder {

    protected abstract fun findBankByNameOrCityForNonEmptyQuery(query: String): List<BankInfo>

    abstract fun searchBankByBic(bic: String): BankInfo?


    protected val cachedBanksByBic = mutableMapOf<String, BankInfo?>()


    override fun findBankByNameBankCodeOrCity(query: String?): List<BankInfo> {
        if (query.isNullOrBlank()) {
            return getBankList()
        }

        if (query.toIntOrNull() != null) { // if query is an integer, then it can only be an bank code, but not a bank name or city
            return findBankByBankCode(query)
        }

        return findBankByNameOrCityForNonEmptyQuery(query)
    }


    override fun findBankByBic(bic: String): BankInfo? {
        cachedBanksByBic[bic]?.let {
            return it
        }

        val bankForBic = searchBankByBic(bic)

        cachedBanksByBic[bic] = bankForBic

        return bankForBic
    }


    protected open fun loadBankList(): List<BankInfo> {
        return BankListDeserializer().loadBankList()
    }

}