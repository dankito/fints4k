package net.dankito.banking.ui.model

import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date


open class RetrievedAccountData(
    open val account: TypedBankAccount,
    open val successfullyRetrievedData: Boolean,
    open val balance: BigDecimal?,
    open val bookedTransactions: Collection<IAccountTransaction>,
    open val unbookedTransactions: List<Any>,
    open val retrievedTransactionsFrom: Date?,
    open val retrievedTransactionsTo: Date?
) {

    companion object {

        fun unsuccessful(account: TypedBankAccount): RetrievedAccountData {
            return RetrievedAccountData(account, false, null, listOf(), listOf(), null, null)
        }

        fun unsuccessfulList(account: TypedBankAccount): List<RetrievedAccountData> {
            return listOf(unsuccessful(account))
        }

    }

}