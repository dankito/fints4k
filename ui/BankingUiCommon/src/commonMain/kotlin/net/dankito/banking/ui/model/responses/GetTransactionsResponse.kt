package net.dankito.banking.ui.model.responses

import net.dankito.banking.ui.model.RetrievedAccountData
import net.dankito.banking.ui.model.TypedBankAccount


open class GetTransactionsResponse(
    errorToShowToUser: String?,
    open val retrievedData: List<RetrievedAccountData>,
    userCancelledAction: Boolean = false,
    open val tanRequiredButWeWereToldToAbortIfSo: Boolean = false
) : BankingClientResponse(true /* any value */, errorToShowToUser, userCancelledAction) {

    constructor(account: TypedBankAccount, errorToShowToUser: String) : this(errorToShowToUser, listOf(RetrievedAccountData(account, false, null, listOf(), listOf())))


    override val isSuccessful: Boolean
        get() = errorToShowToUser == null && retrievedData.isNotEmpty() && retrievedData.none { it.successfullyRetrievedData == false }

}
