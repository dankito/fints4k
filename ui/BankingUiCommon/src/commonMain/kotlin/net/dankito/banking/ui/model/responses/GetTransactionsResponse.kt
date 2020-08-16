package net.dankito.banking.ui.model.responses

import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount


open class GetTransactionsResponse(
    val bankAccount: BankAccount,
    isSuccessful: Boolean,
    errorToShowToUser: String?,
    val bookedTransactions: List<AccountTransaction> = listOf(),
    val unbookedTransactions: List<Any> = listOf(),
    val balance: BigDecimal? = null,
    userCancelledAction: Boolean = false,
    val tanRequiredButWeWereToldToAbortIfSo: Boolean = false
)
    : BankingClientResponse(isSuccessful, errorToShowToUser, userCancelledAction)
