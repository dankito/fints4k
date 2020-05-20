package net.dankito.banking.ui.model.parameters

import net.dankito.banking.ui.model.AccountTransaction
import java.util.*


open class GetTransactionsParameter(
    val alsoRetrieveBalance: Boolean = true,
    val fromDate: Date? = null,
    val toDate: Date? = null,
    val retrievedChunkListener: ((List<AccountTransaction>) -> Unit)? = null
) {

    constructor() : this(true, null, null) // for Java

}