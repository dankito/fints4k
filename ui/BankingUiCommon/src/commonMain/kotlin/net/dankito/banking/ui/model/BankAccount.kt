package net.dankito.banking.ui.model

import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.UUID
import kotlin.jvm.JvmOverloads


open class BankAccount @JvmOverloads constructor(
    open val customer: Customer,
    open var identifier: String,
    open var accountHolderName: String,
    open var iban: String?,
    open var subAccountNumber: String?,
    open var customerId: String,
    open var balance: BigDecimal = BigDecimal.Zero,
    open var currency: String = "EUR",
    open var type: BankAccountType = BankAccountType.Girokonto,
    open var productName: String? = null,
    open var accountLimit: String? = null,
    open var lastRetrievedTransactionsTimestamp: Date? = null,
    open var supportsRetrievingAccountTransactions: Boolean = false,
    open var supportsRetrievingBalance: Boolean = false,
    open var supportsTransferringMoney: Boolean = false,
    open var supportsInstantPaymentMoneyTransfer: Boolean = false,
    open var bookedTransactions: List<AccountTransaction> = listOf(),
    open var unbookedTransactions: List<Any> = listOf()
) {

    internal constructor() : this(Customer(), null, "") // for object deserializers

    /*      convenience constructors for languages not supporting default values        */

    constructor(customer: Customer, productName: String?, identifier: String) : this(customer, productName, identifier, BankAccountType.Girokonto)

    constructor(customer: Customer, productName: String?, identifier: String, type: BankAccountType = BankAccountType.Girokonto, balance: BigDecimal = BigDecimal.Zero)
            : this(customer, identifier, "", null, null, "", balance, "EUR", type, productName)


    open var technicalId: String = UUID.random()


    open var haveAllTransactionsBeenFetched: Boolean = false


    open var userSetDisplayName: String? = null

    open val displayName: String
        get() {
            return userSetDisplayName ?: productName ?: subAccountNumber ?: identifier
        }


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