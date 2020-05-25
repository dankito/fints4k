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
    val bookedTransactionsOfLast90Days: Map<BankAccount, List<AccountTransaction>> = mapOf(),
    val unbookedTransactionsOfLast90Days: Map<BankAccount, List<Any>> = mapOf(),
    val balances: Map<BankAccount, BigDecimal> = mapOf(),
    error: Exception? = null,
    userCancelledAction: Boolean = false
)
    : BankingClientResponse(isSuccessful, errorToShowToUser, error, userCancelledAction) {

    override fun toString(): String {
        return account.toString() + " " + super.toString()
    }

}