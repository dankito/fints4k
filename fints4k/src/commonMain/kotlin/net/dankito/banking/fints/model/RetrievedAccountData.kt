package net.dankito.banking.fints.model

import net.dankito.utils.multiplatform.Date


open class RetrievedAccountData(
    open val accountData: AccountData,
    open val successfullyRetrievedData: Boolean,
    open val balance: Money?,
    open var bookedTransactions: Collection<AccountTransaction>,
    open var unbookedTransactions: Collection<Any>,
    open val retrievedTransactionsFrom: Date?,
    open val retrievedTransactionsTo: Date?
) {

    companion object {

        fun unsuccessful(account: AccountData): RetrievedAccountData {
            return RetrievedAccountData(account, false, null, listOf(), listOf(), null, null)
        }

        fun unsuccessfulList(account: AccountData): List<RetrievedAccountData> {
            return listOf(unsuccessful(account))
        }

    }
}