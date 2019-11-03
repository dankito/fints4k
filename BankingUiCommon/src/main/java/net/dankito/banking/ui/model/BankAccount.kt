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
    bookedAccountTransactions: List<AccountTransaction> = listOf()
) {


    internal constructor() : this(Account(), "", "", null, null) // for object deserializers


    var bookedTransactions: List<AccountTransaction> = bookedAccountTransactions
        protected set

    var unbookedTransactions: List<Any> = listOf()
        protected set


    open fun addBookedTransactions(retrievedBookedTransactions: List<AccountTransaction>) {
        val uniqueTransactions = this.bookedTransactions.toMutableSet()

        uniqueTransactions.addAll(retrievedBookedTransactions)

        this.bookedTransactions = uniqueTransactions.toList()
    }

    open fun addUnbookedTransactions(retrievedUnbookedTransactions: List<Any>) {
        val uniqueUnbookedTransactions = this.unbookedTransactions.toMutableSet()

        uniqueUnbookedTransactions.addAll(retrievedUnbookedTransactions)

        this.unbookedTransactions = uniqueUnbookedTransactions.toList()
    }


    override fun toString(): String {
        return "$name ($identifier)"
    }

}