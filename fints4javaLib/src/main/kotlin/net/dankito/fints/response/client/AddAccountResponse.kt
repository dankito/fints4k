package net.dankito.fints.response.client

import net.dankito.fints.model.AccountTransaction
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData
import net.dankito.fints.response.Response
import java.math.BigDecimal


open class AddAccountResponse(
    response: Response,
    val bank: BankData,
    val customer: CustomerData,
    val supportsRetrievingTransactionsOfLast90DaysWithoutTan: Boolean = false,
    val bookedTransactionsOfLast90Days: List<AccountTransaction> = listOf(),
    val unbookedTransactionsOfLast90Days: List<Any> = listOf(),
    val balance: BigDecimal? = null
)
    : FinTsClientResponse(response)