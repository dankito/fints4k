package net.dankito.banking.ui.model

import java.math.BigDecimal


open class BankAccount @JvmOverloads constructor(
    val account: Account,
    val identifier: String,
    var name: String,
    var iban: String?,
    var subAccountNumber: String?,
    var balance: BigDecimal = BigDecimal.ZERO,
    var currency: String = "EUR",
    var type: BankAccountType = BankAccountType.Giro,
    accountTransactions: List<AccountTransaction> = listOf()
) {


    internal constructor() : this(Account(), "", "", null, null) // for object deserializers


    var transactions: List<AccountTransaction> = accountTransactions
        protected set


    override fun toString(): String {
        return "$name ($identifier)"
    }

}