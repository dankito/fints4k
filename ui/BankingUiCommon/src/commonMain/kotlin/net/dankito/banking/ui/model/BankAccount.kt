package net.dankito.banking.ui.model

//import com.fasterxml.jackson.annotation.JsonIdentityInfo
//import com.fasterxml.jackson.annotation.ObjectIdGenerators
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.UUID
import kotlin.jvm.JvmOverloads


//@JsonIdentityInfo(property = "id", generator = ObjectIdGenerators.PropertyGenerator::class) // to avoid stack overflow due to circular references
open class BankAccount @JvmOverloads constructor(
    open val customer: Customer,
    open val identifier: String,
    open var accountHolderName: String,
    open var iban: String?,
    open var subAccountNumber: String?,
    open val customerId: String,
    open var balance: BigDecimal = BigDecimal.Zero,
    open var currency: String = "EUR",
    open var type: BankAccountType = BankAccountType.Girokonto,
    open val productName: String? = null,
    open val accountLimit: String? = null,
    open var lastRetrievedTransactionsTimestamp: Date? = null,
    open var supportsRetrievingAccountTransactions: Boolean = false,
    open var supportsRetrievingBalance: Boolean = false,
    open var supportsTransferringMoney: Boolean = false,
    open var supportsInstantPaymentMoneyTransfer: Boolean = false,
    bookedAccountTransactions: List<AccountTransaction> = listOf()
) {

    internal constructor() : this(Customer(), "", "", null, null, "") // for object deserializers


    open var id: String = UUID.random()


    open val displayName: String
        get() {
            val productName = productName ?: subAccountNumber

            if (productName != null) {
                return productName + " ($identifier)"
            }


            return identifier
        }

    open val displayNameIncludingBankName: String
        get() = "${customer.bankName} ${displayName}"


    open var bookedTransactions: List<AccountTransaction> = bookedAccountTransactions
        protected set

    open var unbookedTransactions: List<Any> = listOf()
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