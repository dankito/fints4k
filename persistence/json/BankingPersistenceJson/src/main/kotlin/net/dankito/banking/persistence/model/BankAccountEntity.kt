package net.dankito.banking.persistence.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.UUID


@JsonIdentityInfo(property = "technicalId", generator = ObjectIdGenerators.PropertyGenerator::class) // to avoid stack overflow due to circular references
// had to define all properties as 'var' 'cause MapStruct cannot handle vals (and cannot use Pozo's mapstruct-kotlin as SerializableBankAccountBuilder would fail with @Context)
open class BankAccountEntity(
    override var bank: BankDataEntity,
    override var identifier: String,
    override var accountHolderName: String,
    override var iban: String?,
    override var subAccountNumber: String?,
    override var balance: BigDecimal = BigDecimal.Zero,
    override var currency: String = "EUR",
    override var type: BankAccountType = BankAccountType.CheckingAccount,
    override var productName: String? = null,
    override var accountLimit: String? = null,
    override var retrievedTransactionsFromOn: Date? = null,
    override var retrievedTransactionsUpTo: Date? = null,
    override var supportsRetrievingAccountTransactions: Boolean = false,
    override var supportsRetrievingBalance: Boolean = false,
    override var supportsTransferringMoney: Boolean = false,
    override var supportsRealTimeTransfer: Boolean = false,
    override var bookedTransactions: List<AccountTransactionEntity> = listOf(),
    override var unbookedTransactions: List<Any> = listOf(),
    override var technicalId: String = UUID.random(),
    override var userSetDisplayName: String? = null,
    override var haveAllTransactionsBeenRetrieved: Boolean = false,
    override var isAccountTypeSupportedByApplication: Boolean = true,
    override var countDaysForWhichTransactionsAreKept: Int? = null,
    override var displayIndex: Int = 0,
    override var hideAccount: Boolean = false,
    override var includeInAutomaticAccountsUpdate: Boolean = true,
    override var doNotShowStrikingFetchAllTransactionsView: Boolean = false

) : IBankAccount<AccountTransactionEntity> {

    internal constructor() : this(BankDataEntity(), "", "", null, null) // for object deserializers


    override fun toString(): String {
        return stringRepresentation
    }

}