package net.dankito.banking.util


interface IBankIconFinder {

    fun findIconForBank(bankName: String): String?

    fun findBankWebsite(bankName: String): String?

}