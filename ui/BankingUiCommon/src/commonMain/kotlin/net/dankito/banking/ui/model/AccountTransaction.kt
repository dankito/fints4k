package net.dankito.banking.ui.model

//import com.fasterxml.jackson.annotation.JsonIdentityInfo
//import com.fasterxml.jackson.annotation.ObjectIdGenerators
import net.dankito.utils.multiplatform.BigDecimal
import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.DateFormatStyle
import net.dankito.utils.multiplatform.DateFormatter


//@JsonIdentityInfo(property = "technicalId", generator = ObjectIdGenerators.PropertyGenerator::class) // to avoid stack overflow due to circular references
open class AccountTransaction(
    open val bankAccount: BankAccount,
    open val amount: BigDecimal,
    open val currency: String,
    open val unparsedUsage: String,
    open val bookingDate: Date,
    open val otherPartyName: String?,
    open val otherPartyBankCode: String?,
    open val otherPartyAccountId: String?,
    open val bookingText: String?,
    open val valueDate: Date,
    open val statementNumber: Int,
    open val sequenceNumber: Int?,
    open val openingBalance: BigDecimal?,
    open val closingBalance: BigDecimal?,

    open val endToEndReference: String?,
    open val customerReference: String?,
    open val mandateReference: String?,
    open val creditorIdentifier: String?,
    open val originatorsIdentificationCode: String?,
    open val compensationAmount: String?,
    open val originalAmount: String?,
    open val sepaUsage: String?,
    open val deviantOriginator: String?,
    open val deviantRecipient: String?,
    open val usageWithNoSpecialType: String?,
    open val primaNotaNumber: String?,
    open val textKeySupplement: String?,

    open val currencyType: String?,
    open val bookingKey: String,
    open val referenceForTheAccountOwner: String,
    open val referenceOfTheAccountServicingInstitution: String?,
    open val supplementaryDetails: String?,

    open val transactionReferenceNumber: String,
    open val relatedReferenceNumber: String?
) {

    companion object {
        val IdDateFormat = DateFormatter("yyyy.MM.dd")
    }


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


    open val technicalId: String = createDefaultId()

    protected fun createDefaultId() =
        "${bankAccount.technicalId} ${IdDateFormat.format(bookingDate)} ${IdDateFormat.format(valueDate)} $amount $currency $unparsedUsage $otherPartyName $otherPartyBankCode $otherPartyAccountId"


    open val showOtherPartyName: Boolean
        get() = otherPartyName.isNullOrBlank() == false /* && type != "ENTGELTABSCHLUSS" && type != "AUSZAHLUNG" */ // TODO

    open val usage: String
        get() = sepaUsage ?: unparsedUsage


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