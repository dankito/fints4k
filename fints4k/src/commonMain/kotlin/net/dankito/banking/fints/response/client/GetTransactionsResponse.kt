package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.model.RetrievedAccountData
import net.dankito.banking.fints.response.Response


open class GetTransactionsResponse(
    response: Response,
    val retrievedData: List<RetrievedAccountData> = listOf(),
    /**
     * This value is only set if [GetTransactionsParameter.maxCountEntries] was set to tell caller if maxCountEntries parameter has been evaluated or not
     */
    var isSettingMaxCountEntriesAllowedByBank: Boolean? = null
) : FinTsClientResponse(response)