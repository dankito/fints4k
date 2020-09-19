package net.dankito.banking.ui.model.parameters

import net.dankito.utils.multiplatform.Date
import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.banking.ui.model.TypedBankAccount
import kotlin.jvm.JvmOverloads


open class GetTransactionsParameter @JvmOverloads constructor(
    open val account: TypedBankAccount,
    open val alsoRetrieveBalance: Boolean = true,
    open val fromDate: Date? = null,
    open val toDate: Date? = null,
    open val abortIfTanIsRequired: Boolean = false,
    open val retrievedChunkListener: ((List<IAccountTransaction>) -> Unit)? = null
)