package net.dankito.banking.persistence.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import net.dankito.banking.ui.model.BankAccountType
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.UUID


@JsonIdentityInfo(property = "id", generator = ObjectIdGenerators.PropertyGenerator::class) // to avoid stack overflow due to circular references
// had to define all properties as 'var' 'cause MapStruct cannot handle vals (and cannot use Pozo's mapstruct-kotlin as SerializableBankAccountBuilder would fail with @Context)
open class BankAccountEntity(
    open var customer: CustomerEntity,
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
    open var bookedTransactions: List<AccountTransactionEntity> = listOf(),
    open var unbookedTransactions: List<Any> = listOf(),
    open var id: String = UUID.random().toString()

) {

    internal constructor() : this(CustomerEntity(), "", "", null, null, "") // for object deserializers

}