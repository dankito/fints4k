package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.model.AccountTransaction
import net.dankito.banking.fints.response.Response
import java.math.BigDecimal


open class GetTransactionsResponse(
    response: Response,
    val bookedTransactions: List<AccountTransaction> = listOf(),
    val unbookedTransactions: List<Any> = listOf(),
    val balance: BigDecimal? = null
)
    : FinTsClientResponse(response)