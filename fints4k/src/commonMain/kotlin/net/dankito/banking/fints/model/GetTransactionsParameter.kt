package net.dankito.banking.fints.model

import com.soywiz.klock.Date


open class GetTransactionsParameter(
    val alsoRetrieveBalance: Boolean = true,
    val fromDate: Date? = null,
    val toDate: Date? = null,
    val maxCountEntries: Int? = null,
    val abortIfTanIsRequired: Boolean = false,
    val retrievedChunkListener: ((List<AccountTransaction>) -> Unit)? = null
)