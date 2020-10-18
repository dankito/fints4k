package net.dankito.banking.util


interface IBankIconFinder {

    fun findIconForBankAsync(bankName: String, prefSize: Int = 72, result: (String?) -> Unit)

    fun findIconForBank(bankName: String, prefSize: Int = 72): String?

}