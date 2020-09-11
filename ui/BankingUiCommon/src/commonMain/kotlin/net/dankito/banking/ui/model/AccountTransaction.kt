package net.dankito.banking.ui.model

import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.DateFormatStyle
import net.dankito.utils.multiplatform.DateFormatter


open class AccountTransaction(
    override val bankAccount: TypedBankAccount,
    override val amount: BigDecimal,
    override val currency: String,
    override val unparsedUsage: String,
    override val bookingDate: Date,
    override val otherPartyName: String?,
    override val otherPartyBankCode: String?,
    override val otherPartyAccountId: String?,
    override val bookingText: String?,
    override val valueDate: Date,
    override val statementNumber: Int,
    override val sequenceNumber: Int?,
    override val openingBalance: BigDecimal?,
    override val closingBalance: BigDecimal?,

    override val endToEndReference: String?,
    override val customerReference: String?,
    override val mandateReference: String?,
    override val creditorIdentifier: String?,
    override val originatorsIdentificationCode: String?,
    override val compensationAmount: String?,
    override val originalAmount: String?,
    override val sepaUsage: String?,
    override val deviantOriginator: String?,
    override val deviantRecipient: String?,
    override val usageWithNoSpecialType: String?,
    override val primaNotaNumber: String?,
    override val textKeySupplement: String?,

    override val currencyType: String?,
    override val bookingKey: String,
    override val referenceForTheAccountOwner: String,
    override val referenceOfTheAccountServicingInstitution: String?,
    override val supplementaryDetails: String?,

    override val transactionReferenceNumber: String,
    override val relatedReferenceNumber: String?
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


    override var technicalId: String = buildTransactionIdentifier()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountTransaction) return false

        if (bankAccount != other.bankAccount) return false
        if (amount != other.amount) return false
        if (currency != other.currency) return false
        if (unparsedUsage != other.unparsedUsage) return false
        if (bookingDate != other.bookingDate) return false
        if (otherPartyName != other.otherPartyName) return false
        if (otherPartyBankCode != other.otherPartyBankCode) return false
        if (otherPartyAccountId != other.otherPartyAccountId) return false
        if (bookingText != other.bookingText) return false
        if (valueDate != other.valueDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bankAccount.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + currency.hashCode()
        result = 31 * result + unparsedUsage.hashCode()
        result = 31 * result + bookingDate.hashCode()
        result = 31 * result + (otherPartyName?.hashCode() ?: 0)
        result = 31 * result + (otherPartyBankCode?.hashCode() ?: 0)
        result = 31 * result + (otherPartyAccountId?.hashCode() ?: 0)
        result = 31 * result + (bookingText?.hashCode() ?: 0)
        result = 31 * result + valueDate.hashCode()
        return result
    }


    override fun toString(): String {
        return "${DateFormatter(DateFormatStyle.Medium).format(valueDate)} $amount $otherPartyName: $usage"
    }

}