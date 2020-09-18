package net.dankito.banking.ui.model.responses

import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.BigDecimal


open class AddAccountResponse(
    isSuccessful: Boolean,
    errorToShowToUser: String?,
    val customer: TypedCustomer,
    val supportsRetrievingTransactionsOfLast90DaysWithoutTan: Boolean = false,
    val retrievedData: List<RetrievedAccountData> = listOf(),
    userCancelledAction: Boolean = false
) : BankingClientResponse(isSuccessful, errorToShowToUser, userCancelledAction) {

    override fun toString(): String {
        return customer.toString() + " " + super.toString()
    }

}