package net.dankito.banking.util


interface IBankIconFinder {

    fun findIconForBank(bankName: String, prefSize: Int = 72): String?

    fun findBankWebsite(bankName: String): String?

}