package net.dankito.banking.ui.model.responses

import net.dankito.banking.ui.model.*


open class AddAccountResponse(
    open val customer: TypedCustomer,
    retrievedData: List<RetrievedAccountData> = listOf(),
    errorToShowToUser: String?,
    userCancelledAction: Boolean = false
) : GetTransactionsResponse(retrievedData, errorToShowToUser, userCancelledAction) {

    constructor(customer: TypedCustomer, errorToShowToUser: String?) : this(customer, listOf(), errorToShowToUser)


    override val successful: Boolean
        get() = super.successful && customer.accounts.isNotEmpty()

    open val supportsRetrievingTransactionsOfLast90DaysWithoutTan: Boolean
        get() = retrievedData.isNotEmpty() && retrievedData.any { it.successfullyRetrievedData }


    override fun toString(): String {
        return customer.toString() + " " + super.toString()
    }

}