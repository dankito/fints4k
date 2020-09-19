package net.dankito.banking.ui.model

import net.dankito.utils.multiplatform.BigDecimal


open class RetrievedAccountData(
    open val account: TypedBankAccount,
    open val successfullyRetrievedData: Boolean,
    open val balance: BigDecimal?,
    open val bookedTransactions: Collection<IAccountTransaction>,
    open val unbookedTransactions: List<Any>
)