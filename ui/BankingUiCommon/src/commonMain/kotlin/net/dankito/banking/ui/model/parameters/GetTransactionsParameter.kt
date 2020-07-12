package net.dankito.banking.ui.model.parameters

import net.dankito.utils.multiplatform.Date
import net.dankito.banking.ui.model.AccountTransaction


open class GetTransactionsParameter(
    val alsoRetrieveBalance: Boolean = true,
    val fromDate: Date? = null,
    val toDate: Date? = null,
    val abortIfTanIsRequired: Boolean = false,
    val retrievedChunkListener: ((List<AccountTransaction>) -> Unit)? = null
) {

    constructor() : this(true, null, null) // for Java

}