package net.dankito.banking.ui.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.math.BigDecimal
import java.text.DateFormat
import java.util.*


@JsonIdentityInfo(property = "id", generator = ObjectIdGenerators.PropertyGenerator::class) // to avoid stack overflow due to circular references
open class AccountTransaction(
    val bankAccount: BankAccount,
    val amount: BigDecimal,
    val currency: String,
    val unparsedUsage: String,
    val bookingDate: Date,
    val otherPartyName: String?,
    val otherPartyBankCode: String?,
    val otherPartyAccountId: String?,
    val bookingText: String?,
    val valueDate: Date,
    val statementNumber: Int,
    val sequenceNumber: Int?,
    val openingBalance: BigDecimal?,
    val closingBalance: BigDecimal?,

    val endToEndReference: String?,
    val customerReference: String?,
    val mandateReference: String?,
    val creditorIdentifier: String?,
    val originatorsIdentificationCode: String?,
    val compensationAmount: String?,
    val originalAmount: String?,
    val sepaUsage: String?,
    val deviantOriginator: String?,
    val deviantRecipient: String?,
    val usageWithNoSpecialType: String?,
    val primaNotaNumber: String?,
    val textKeySupplement: String?,

    val currencyType: String?,
    val bookingKey: String,
    val referenceForTheAccountOwner: String,
    val referenceOfTheAccountServicingInstitution: String?,
    val supplementaryDetails: String?,

    val transactionReferenceNumber: String,
    val relatedReferenceNumber: String?
) {

    constructor(bankAccount: BankAccount, amount: BigDecimal, unparsedUsage: String, bookingDate: Date,
                otherPartyName: String?, otherPartyBankCode: String?, otherPartyAccountId: String?,
                bookingText: String?, valueDate: Date)
            : this(bankAccount, amount, "EUR", unparsedUsage, bookingDate,
                    otherPartyName, otherPartyBankCode, otherPartyAccountId, bookingText, valueDate,
    0, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "", "", null, null, "", null)

    // for object deserializers
    internal constructor() : this(BankAccount(), BigDecimal.ZERO, "","", Date(), null, null, null, null, Date(), 0, null, BigDecimal.ZERO, BigDecimal.ZERO,
    null, null, null, null, null, null, null, null, null, null, null, null, null,
    null, "", "", null, null, "", null)


    var id: String = UUID.randomUUID().toString()
        protected set


    val showOtherPartyName: Boolean
        get() = otherPartyName.isNullOrBlank() == false /* && type != "ENTGELTABSCHLUSS" && type != "AUSZAHLUNG" */ // TODO

    val usage: String
        get() = sepaUsage ?: unparsedUsage


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountTransaction) return false

        if (bankAccount != other.bankAccount) return false
        if (amount.compareTo(other.amount) != 0) return false
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
        return "${DateFormat.getDateInstance(DateFormat.MEDIUM).format(bookingDate)} $amount $otherPartyName: $usage"
    }

}