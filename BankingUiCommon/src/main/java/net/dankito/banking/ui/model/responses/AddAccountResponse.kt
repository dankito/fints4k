package net.dankito.banking.ui.model.responses

import net.dankito.banking.ui.model.Account
import net.dankito.banking.ui.model.AccountTransaction
import net.dankito.banking.ui.model.BankAccount
import java.math.BigDecimal


open class AddAccountResponse(
    isSuccessful: Boolean,
    errorToShowToUser: String?,
    val account: Account,
    val supportsRetrievingTransactionsOfLast90DaysWithoutTan: Boolean = false,
    bookedTransactionsOfLast90Days: Map<BankAccount, List<AccountTransaction>> = mapOf(),
    unbookedTransactionsOfLast90Days: Map<BankAccount, List<Any>> = mapOf(),
    balances: Map<BankAccount, BigDecimal> = mapOf(),
    error: Exception? = null
)
    : GetTransactionsResponse(isSuccessful, errorToShowToUser, bookedTransactionsOfLast90Days, unbookedTransactionsOfLast90Days, balances, error) {

    override fun toString(): String {
        return account.toString() + " " + super.toString()
    }

}