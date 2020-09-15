package net.dankito.banking.persistence.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import net.dankito.banking.persistence.dao.BaseDao
import net.dankito.banking.ui.model.IAccountTransaction
import net.dankito.utils.multiplatform.*


@Entity
open class AccountTransaction(
    @Ignore
    override var bankAccount: BankAccount,

    override var amount: BigDecimal,
    override var currency: String,
    override var unparsedUsage: String,
    override var bookingDate: Date,
    override var otherPartyName: String?,
    override var otherPartyBankCode: String?,
    override var otherPartyAccountId: String?,
    override var bookingText: String?,
    override var valueDate: Date,
    override var statementNumber: Int,
    override var sequenceNumber: Int?,
    override var openingBalance: BigDecimal?,
    override var closingBalance: BigDecimal?,

    override var endToEndReference: String?,
    override var customerReference: String?,
    override var mandateReference: String?,
    override var creditorIdentifier: String?,
    override var originatorsIdentificationCode: String?,
    override var compensationAmount: String?,
    override var originalAmount: String?,
    override var sepaUsage: String?,
    override var deviantOriginator: String?,
    override var deviantRecipient: String?,
    override var usageWithNoSpecialType: String?,
    override var primaNotaNumber: String?,
    override var textKeySupplement: String?,

    override var currencyType: String?,
    override var bookingKey: String,
    override var referenceForTheAccountOwner: String,
    override var referenceOfTheAccountServicingInstitution: String?,
    override var supplementaryDetails: String?,

    override var transactionReferenceNumber: String,
    override var relatedReferenceNumber: String?
) : IAccountTransaction {

    // for object deserializers
    internal constructor() : this(BankAccount(), null, "", BigDecimal.Zero, Date(), null)

    /*      convenience constructors for languages not supporting default values        */

    constructor(bankAccount: BankAccount, otherPartyName: String?, unparsedUsage: String, amount: BigDecimal, valueDate: Date, bookingText: String?)
            : this(bankAccount, amount, "EUR", unparsedUsage, valueDate,
        otherPartyName, null, null, bookingText, valueDate)


    constructor(bankAccount: BankAccount, amount: BigDecimal, currency: String, unparsedUsage: String, bookingDate: Date,
                otherPartyName: String?, otherPartyBankCode: String?, otherPartyAccountId: String?,
                bookingText: String?, valueDate: Date)
            : this(bankAccount, amount, currency, unparsedUsage, bookingDate,
        otherPartyName, otherPartyBankCode, otherPartyAccountId, bookingText, valueDate,
        0, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "", "", null, null, "", null)


    @PrimaryKey(autoGenerate = true)
    open var id: Long = BaseDao.IdNotSet

    override var technicalId: String = buildTransactionIdentifier()

    // Room doesn't allow me to add getters and setters -> have to map it manually
    open var bankAccountId: Long = BaseDao.ObjectNotInsertedId


    override fun equals(other: Any?): Boolean {
        return doesEqual(other)
    }

    override fun hashCode(): Int {
        return calculateHashCode()
    }


    override fun toString(): String {
        return stringRepresentation
    }

}