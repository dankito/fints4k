package net.dankito.fints.response.client

import net.dankito.fints.model.AccountTransaction
import net.dankito.fints.response.Response
import java.math.BigDecimal


open class GetTransactionsResponse(
    response: Response,
    val bookedTransactions: List<AccountTransaction> = listOf(),
    val unbookedTransactions: List<Any> = listOf(),
    val balance: BigDecimal? = null
)
    : ClientResponseBase(response)