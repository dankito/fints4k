package net.dankito.banking.fints.banks

import net.dankito.banking.fints.model.BankInfo


interface IBankFinder {

    fun getBankList(): List<BankInfo>

    fun findBankByBankCode(query: String): List<BankInfo>

    fun findBankByNameBankCodeOrCity(query: String?): List<BankInfo>

    fun preloadBankList()

}