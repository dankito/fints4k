package net.dankito.banking.fints.response.client

import net.dankito.banking.fints.model.AccountTransaction
import net.dankito.banking.fints.model.Money
import net.dankito.banking.fints.response.Response


open class GetTransactionsResponse(
    response: Response,
    val bookedTransactions: List<AccountTransaction> = listOf(),
    val unbookedTransactions: List<Any> = listOf(),
    val balance: Money? = null,
    /**
     * This value is only set if [GetTransactionsParameter.maxCountEntries] was set to tell caller if maxCountEntries parameter has been evaluated or not
     */
    var isSettingMaxCountEntriesAllowedByBank: Boolean? = null
) : FinTsClientResponse(response)