package net.dankito.banking.fints.response.client

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import net.dankito.banking.fints.model.*
import net.dankito.banking.fints.response.Response


open class AddAccountResponse(
    response: Response,
    val bank: BankData,
    val customer: CustomerData,
    val supportsRetrievingTransactionsOfLast90DaysWithoutTan: Boolean = false,
    bookedTransactionsOfLast90Days: List<AccountTransaction> = listOf(),
    unbookedTransactionsOfLast90Days: List<Any> = listOf(),
    val balances: Map<AccountData, Money> = mapOf()
)
    : GetTransactionsResponse(response, bookedTransactionsOfLast90Days, unbookedTransactionsOfLast90Days,
    Money(balances.values.fold(BigDecimal.ZERO) { acc, e -> acc + e.amount }, balances.values.firstOrNull()?.currency?.code ?: "EUR"))