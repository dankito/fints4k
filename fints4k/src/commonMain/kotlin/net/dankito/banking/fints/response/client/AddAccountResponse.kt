package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.model.*
import net.dankito.banking.fints.response.Response


open class AddAccountResponse(
    response: Response,
    open val bank: BankData,
    open val supportsRetrievingTransactionsOfLast90DaysWithoutTan: Boolean = false,
    retrievedData: List<RetrievedAccountData> = listOf()
) : GetTransactionsResponse(response, retrievedData)