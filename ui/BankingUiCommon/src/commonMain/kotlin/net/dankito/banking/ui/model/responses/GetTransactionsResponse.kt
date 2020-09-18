package net.dankito.banking.ui.model.responses

import net.dankito.banking.ui.model.RetrievedAccountData
import net.dankito.banking.ui.model.TypedBankAccount


open class GetTransactionsResponse(
    open val bankAccount: TypedBankAccount,
    isSuccessful: Boolean,
    errorToShowToUser: String?,
    open val retrievedData: List<RetrievedAccountData> = listOf(),
    userCancelledAction: Boolean = false,
    open val tanRequiredButWeWereToldToAbortIfSo: Boolean = false
) : BankingClientResponse(isSuccessful, errorToShowToUser, userCancelledAction)
