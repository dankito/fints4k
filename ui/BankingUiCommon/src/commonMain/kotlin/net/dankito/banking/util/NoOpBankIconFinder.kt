package net.dankito.banking.util


open class NoOpBankIconFinder : IBankIconFinder {

    override fun findIconForBank(bankName: String, prefSize: Int): String? {
        return null
    }

}