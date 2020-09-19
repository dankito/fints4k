package net.dankito.banking.ui.model.responses

import net.dankito.banking.ui.model.RetrievedAccountData
import net.dankito.banking.ui.model.TypedBankAccount


open class GetTransactionsResponse(
    open val retrievedData: List<RetrievedAccountData>,
    errorToShowToUser: String?,
    userCancelledAction: Boolean = false,
    open val tanRequiredButWeWereToldToAbortIfSo: Boolean = false
) : BankingClientResponse(true /* any value */, errorToShowToUser, userCancelledAction) {

    constructor(account: TypedBankAccount, errorToShowToUser: String) : this(listOf(RetrievedAccountData(account, false, null, listOf(), listOf())), errorToShowToUser)

    constructor(retrievedData: RetrievedAccountData) : this(listOf(retrievedData))

    constructor(retrievedData: List<RetrievedAccountData>) : this(retrievedData, null)


    override val successful: Boolean
        get() = errorToShowToUser == null && retrievedData.isNotEmpty() && retrievedData.none { it.successfullyRetrievedData == false }

}
