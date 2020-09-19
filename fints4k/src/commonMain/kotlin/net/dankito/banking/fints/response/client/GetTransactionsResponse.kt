package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.model.RetrievedAccountData
import net.dankito.banking.fints.response.Response


open class GetTransactionsResponse(
    response: Response,
    open val retrievedData: List<RetrievedAccountData> = listOf(),
    /**
     * This value is only set if [GetTransactionsParameter.maxCountEntries] was set to tell caller if maxCountEntries parameter has been evaluated or not
     */
    open var isSettingMaxCountEntriesAllowedByBank: Boolean? = null
) : FinTsClientResponse(response) {

    override val isSuccessful: Boolean
        get() = super.isSuccessful && retrievedData.isNotEmpty() && retrievedData.none { it.successfullyRetrievedData == false }

}