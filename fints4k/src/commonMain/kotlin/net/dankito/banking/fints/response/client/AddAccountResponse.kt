package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.model.*
import net.dankito.banking.fints.response.BankResponse


open class AddAccountResponse(
    response: BankResponse,
    open val bank: BankData,
    retrievedData: List<RetrievedAccountData> = listOf()
) : GetTransactionsResponse(response, retrievedData) {

    override val successful: Boolean
        get() = super.successful && bank.accounts.isNotEmpty()

}