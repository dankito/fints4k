package net.dankito.banking.ui.model.responses

import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.BigDecimal


open class AddAccountResponse(
    isSuccessful: Boolean,
    errorToShowToUser: String?,
    val customer: TypedCustomer,
    val supportsRetrievingTransactionsOfLast90DaysWithoutTan: Boolean = false,
    val bookedTransactionsOfLast90Days: Map<TypedBankAccount, List<IAccountTransaction>> = mapOf(),
    val unbookedTransactionsOfLast90Days: Map<TypedBankAccount, List<Any>> = mapOf(),
    val balances: Map<TypedBankAccount, BigDecimal> = mapOf(),
    userCancelledAction: Boolean = false
)
    : BankingClientResponse(isSuccessful, errorToShowToUser, userCancelledAction) {

    override fun toString(): String {
        return customer.toString() + " " + super.toString()
    }

}