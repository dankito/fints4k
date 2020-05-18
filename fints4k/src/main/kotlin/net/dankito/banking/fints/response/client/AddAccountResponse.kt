package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.model.AccountData
import net.dankito.banking.fints.model.AccountTransaction
import net.dankito.banking.fints.model.BankData
import net.dankito.banking.fints.model.CustomerData
import net.dankito.banking.fints.response.Response
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