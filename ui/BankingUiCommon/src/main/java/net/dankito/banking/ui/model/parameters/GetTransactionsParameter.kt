package net.dankito.banking.ui.model.parameters

import java.util.*


open class GetTransactionsParameter(
    val alsoRetrieveBalance: Boolean = true,
    val fromDate: Date? = null,
    val toDate: Date? = null
) {

    constructor() : this(true, null, null) // for Java

}