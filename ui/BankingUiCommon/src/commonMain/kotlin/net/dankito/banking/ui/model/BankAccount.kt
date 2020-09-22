package net.dankito.banking.ui.model

import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.UUID
import kotlin.jvm.JvmOverloads


open class BankAccount @JvmOverloads constructor(
    override val customer: TypedCustomer,
    override var identifier: String,
    override var accountHolderName: String,
    override var iban: String?,
    override var subAccountNumber: String?,
    override var customerId: String,
    override var balance: BigDecimal = BigDecimal.Zero,
    override var currency: String = "EUR",
    override var type: BankAccountType = BankAccountType.Girokonto,
    override var productName: String? = null,
    override var accountLimit: String? = null,
    override var retrievedTransactionsFromOn: Date? = null,
    override var retrievedTransactionsUpTo: Date? = null,
    override var supportsRetrievingAccountTransactions: Boolean = false,
    override var supportsRetrievingBalance: Boolean = false,
    override var supportsTransferringMoney: Boolean = false,
    override var supportsInstantPaymentMoneyTransfer: Boolean = false,
    override var bookedTransactions: List<IAccountTransaction> = listOf(),
    override var unbookedTransactions: List<Any> = listOf()
) : TypedBankAccount {

    internal constructor() : this(Customer(), null, "") // for object deserializers

    /*      convenience constructors for languages not supporting default values        */

    constructor(customer: TypedCustomer, productName: String?, identifier: String) : this(customer, productName, identifier, BankAccountType.Girokonto)

    constructor(customer: TypedCustomer, productName: String?, identifier: String, type: BankAccountType = BankAccountType.Girokonto, balance: BigDecimal = BigDecimal.Zero)
            : this(customer, identifier, "", null, null, "", balance, "EUR", type, productName)


    override var technicalId: String = UUID.random()


    override var haveAllTransactionsBeenFetched: Boolean = false

    override var isAccountTypeSupported: Boolean = true


    override var userSetDisplayName: String? = null

    override var displayIndex: Int = 0


    override fun toString(): String {
        return stringRepresentation
    }

}