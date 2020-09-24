package net.dankito.banking.persistence.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import net.dankito.banking.persistence.dao.BaseDao
import net.dankito.banking.ui.model.*
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.UUID


@Entity
open class BankAccount(
    @Ignore
    override var bank: TypedBankData,
    override var identifier: String,
    override var accountHolderName: String,
    override var iban: String?,
    override var subAccountNumber: String?,
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
    override var supportsRealTimeTransfer: Boolean = false,

    @Ignore
    override var bookedTransactions: List<IAccountTransaction> = listOf(),
    @Ignore
    override var unbookedTransactions: List<Any> = listOf()
) : TypedBankAccount {

    internal constructor() : this(Bank(), null, "") // for object deserializers

    /*      convenience constructors for languages not supporting default values        */

    constructor(bank: TypedBankData, productName: String?, identifier: String) : this(bank, productName, identifier, BankAccountType.Girokonto)

    constructor(bank: TypedBankData, productName: String?, identifier: String, type: BankAccountType = BankAccountType.Girokonto, balance: BigDecimal = BigDecimal.Zero)
            : this(bank, identifier, "", null, null, balance, "EUR", type, productName)


    @PrimaryKey(autoGenerate = true)
    open var id: Long = BaseDao.IdNotSet

    override var technicalId: String = UUID.random()

    // Room doesn't allow me to add getters and setters -> have to map it manually
    open var bankId: Long = BaseDao.ObjectNotInsertedId


    override var haveAllTransactionsBeenRetrieved: Boolean = false

    override var isAccountTypeSupportedByApplication: Boolean = true


    override var userSetDisplayName: String? = null

    override var displayIndex: Int = 0


    override fun toString(): String {
        return stringRepresentation
    }

}