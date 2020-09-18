package net.dankito.banking.ui.model

import net.dankito.utils.multiplatform.BigDecimal


open class RetrievedAccountData(
    val account: TypedBankAccount,
    val balance: BigDecimal?,
    var bookedTransactions: Collection<IAccountTransaction>,
    var unbookedTransactions: List<Any>
)