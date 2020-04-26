package net.dankito.banking.util

import net.dankito.fints.model.BankInfo


interface IBankIconFinder {

    fun findIconForBank(bankInfo: BankInfo): String?

    fun findIconForBank(bankName: String): String?

    fun findBankWebsite(bankName: String): String?

}