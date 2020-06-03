package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.model.AccountTransaction
import net.dankito.banking.fints.model.Money
import net.dankito.banking.fints.response.Response


open class GetTransactionsResponse(
    response: Response,
    val bookedTransactions: List<AccountTransaction> = listOf(),
    val unbookedTransactions: List<Any> = listOf(),
    val balance: Money? = null
)
    : FinTsClientResponse(response)