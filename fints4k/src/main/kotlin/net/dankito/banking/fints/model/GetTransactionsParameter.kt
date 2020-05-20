package net.dankito.banking.fints.model

import java.util.*


open class GetTransactionsParameter @JvmOverloads constructor(
    val alsoRetrieveBalance: Boolean = true,
    val fromDate: Date? = null,
    val toDate: Date? = null,
    val maxCountEntries: Int? = null,
    val retrievedChunkListener: ((List<AccountTransaction>) -> Unit)? = null
)