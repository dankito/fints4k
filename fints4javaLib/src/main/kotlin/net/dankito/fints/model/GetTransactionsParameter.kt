package net.dankito.fints.model

import java.util.*


open class GetTransactionsParameter @JvmOverloads constructor(
    val alsoRetrieveBalance: Boolean = true,
    val fromDate: Date? = null,
    val toDate: Date? = null,
    val maxAmount: Int? = null,
    val allAccounts: Boolean = false,
    val continuationId: String? = null
)