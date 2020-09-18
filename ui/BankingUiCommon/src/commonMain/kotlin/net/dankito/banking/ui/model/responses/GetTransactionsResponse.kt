package net.dankito.banking.ui.model.responses

import net.dankito.banking.ui.model.RetrievedAccountData
import net.dankito.banking.ui.model.TypedBankAccount


open class GetTransactionsResponse(
    val bankAccount: TypedBankAccount,
    isSuccessful: Boolean,
    errorToShowToUser: String?,
    val retrievedData: List<RetrievedAccountData> = listOf(),
    userCancelledAction: Boolean = false,
    val tanRequiredButWeWereToldToAbortIfSo: Boolean = false
) : BankingClientResponse(isSuccessful, errorToShowToUser, userCancelledAction)
