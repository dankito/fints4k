package net.dankito.banking.ui.model.responses

import net.dankito.banking.ui.model.RetrievedAccountData


open class GetTransactionsResponse(
    errorToShowToUser: String?,
    open val retrievedData: List<RetrievedAccountData> = listOf(),
    userCancelledAction: Boolean = false,
    open val tanRequiredButWeWereToldToAbortIfSo: Boolean = false
) : BankingClientResponse(true /* any value */, errorToShowToUser, userCancelledAction) {

    override val isSuccessful: Boolean
        get() = errorToShowToUser == null && retrievedData.isNotEmpty() && retrievedData.none { it.successfullyRetrievedData == false }

}
