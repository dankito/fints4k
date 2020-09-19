package net.dankito.banking.ui.model.responses

import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.BigDecimal


open class AddAccountResponse(
    isSuccessful: Boolean,
    errorToShowToUser: String?,
    open val customer: TypedCustomer,
    retrievedData: List<RetrievedAccountData> = listOf(),
    userCancelledAction: Boolean = false
) : GetTransactionsResponse(isSuccessful, errorToShowToUser, retrievedData, userCancelledAction) {

    open val supportsRetrievingTransactionsOfLast90DaysWithoutTan: Boolean
        get() = retrievedData.isNotEmpty() && retrievedData.any { it.successfullyRetrievedData }


    override fun toString(): String {
        return customer.toString() + " " + super.toString()
    }

}