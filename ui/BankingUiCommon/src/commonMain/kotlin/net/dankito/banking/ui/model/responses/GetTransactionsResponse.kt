package net.dankito.banking.ui.model.responses

import net.dankito.banking.ui.model.RetrievedAccountData
import net.dankito.banking.ui.model.TypedBankAccount


open class GetTransactionsResponse(
    open val retrievedData: List<RetrievedAccountData>,
    errorToShowToUser: String?,
    wrongCredentialsEntered: Boolean = false,
    userCancelledAction: Boolean = false,
    open val tanRequiredButWeWereToldToAbortIfSo: Boolean = false
) : BankingClientResponse(true /* any value */, errorToShowToUser, wrongCredentialsEntered, userCancelledAction) {

    constructor(account: TypedBankAccount, errorToShowToUser: String) : this(RetrievedAccountData.unsuccessfulList(account), errorToShowToUser)

    constructor(account: TypedBankAccount, response: BankingClientResponse) : this(RetrievedAccountData.unsuccessfulList(account), response.errorToShowToUser,
        response.wrongCredentialsEntered, response.userCancelledAction, (response as? GetTransactionsResponse)?.tanRequiredButWeWereToldToAbortIfSo ?: false)

    constructor(retrievedData: RetrievedAccountData) : this(listOf(retrievedData))

    constructor(retrievedData: List<RetrievedAccountData>) : this(retrievedData, null)


    override val successful: Boolean
        get() = errorToShowToUser == null
                && wrongCredentialsEntered == false
                && retrievedData.isNotEmpty()
                && retrievedData.none { it.account.supportsRetrievingAccountTransactions && it.successfullyRetrievedData == false }

}
