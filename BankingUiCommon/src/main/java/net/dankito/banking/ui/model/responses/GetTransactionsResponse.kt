package net.dankito.banking.ui.model.responses

import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import java.math.BigDecimal


open class GetTransactionsResponse(
    isSuccessful: Boolean,
    errorToShowToUser: String?,
    val bookedTransactions: Map<BankAccount, List<AccountTransaction>> = mapOf(),
    val unbookedTransactions: Map<BankAccount, List<Any>> = mapOf(),
    val balances: Map<BankAccount, BigDecimal> = mapOf(),
    error: Exception? = null
)
    : BankingClientResponse(isSuccessful, errorToShowToUser, error)
