package net.dankito.banking.util


open class NoOpBankIconFinder : IBankIconFinder {

    override fun findIconForBankAsync(bankName: String, prefSize: Int, result: (String?) -> Unit) {
        result(findIconForBank(bankName, prefSize))
    }

    override fun findIconForBank(bankName: String, prefSize: Int): String? {
        return null
    }

}