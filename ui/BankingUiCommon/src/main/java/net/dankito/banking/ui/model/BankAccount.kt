package net.dankito.banking.ui.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.math.BigDecimal
import java.util.*


@JsonIdentityInfo(property = "id", generator = ObjectIdGenerators.PropertyGenerator::class) // to avoid stack overflow due to circular references
open class BankAccount @JvmOverloads constructor(
    val account: Account,
    val identifier: String,
    var accountHolderName: String,
    var iban: String?,
    var subAccountNumber: String?,
    var balance: BigDecimal = BigDecimal.ZERO,
    var currency: String = "EUR",
    var type: BankAccountType = BankAccountType.Girokonto,
    var lastRetrievedTransactionsTimestamp: Date? = null,
    var supportsRetrievingAccountTransactions: Boolean = false,
    var supportsRetrievingBalance: Boolean = false,
    var supportsTransferringMoney: Boolean = false,
    var supportsInstantPaymentMoneyTransfer: Boolean = false,
    bookedAccountTransactions: List<AccountTransaction> = listOf()
) {

    internal constructor() : this(Account(), "", "", null, null) // for object deserializers


    var id: String = UUID.randomUUID().toString()
        protected set


    val displayName: String
        get() {
            var displayName = identifier
            subAccountNumber?.let {
                displayName += " ($it)"
            }

            return displayName
        }

    val displayNameIncludingBankName: String
        get() = "${account.bank.name} ${displayName}"


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
        return "$accountHolderName ($identifier)"
    }

}