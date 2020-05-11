package net.dankito.fints.response.client

import net.dankito.fints.model.AccountData
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
    bookedTransactionsOfLast90Days: List<AccountTransaction> = listOf(),
    unbookedTransactionsOfLast90Days: List<Any> = listOf(),
    val balances: Map<AccountData, BigDecimal> = mapOf()
)
    : GetTransactionsResponse(response, bookedTransactionsOfLast90Days, unbookedTransactionsOfLast90Days, balances.values.fold(BigDecimal.ZERO) { acc, e -> acc + e })