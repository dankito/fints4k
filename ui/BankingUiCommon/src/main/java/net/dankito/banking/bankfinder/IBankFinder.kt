package net.dankito.banking.bankfinder


interface IBankFinder {

    fun getBankList(): List<BankInfo>

    fun findBankByBankCode(query: String): List<BankInfo>

    fun findBankByNameBankCodeOrCity(query: String?): List<BankInfo>

    fun preloadBankList()

}