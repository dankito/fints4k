package net.dankito.banking.fints.model

import net.dankito.utils.multiplatform.Date


open class AccountTransaction(
    val account: AccountData,
    val amount: Money,
    val isReversal: Boolean,
    val unparsedUsage: String,
    val bookingDate: Date,
    val otherPartyName: String?,
    val otherPartyBankCode: String?,
    val otherPartyAccountId: String?,
    val bookingText: String?,
    val valueDate: Date,
    val statementNumber: Int,
    val sequenceNumber: Int?,
    val openingBalance: Money?,
    val closingBalance: Money?,

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

    // for object deserializers
    internal constructor() : this(AccountData(), Money(Amount.Zero, ""), "", Date(0), null, null, null, null, Date(0))

    constructor(account: AccountData, amount: Money, unparsedUsage: String, bookingDate: Date, otherPartyName: String?, otherPartyBankCode: String?, otherPartyAccountId: String?, bookingText: String?, valueDate: Date)
        : this(account, amount, false, unparsedUsage, bookingDate, otherPartyName, otherPartyBankCode, otherPartyAccountId, bookingText, valueDate,
        0, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null,  null, null,
        null, "", "", null, null, "", null)


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountTransaction) return false

        if (account != other.account) return false
        if (amount != other.amount) return false
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
        var result = account.hashCode()
        result = 31 * result + amount.hashCode()
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
        return "$valueDate $amount $otherPartyName: $unparsedUsage"
    }

}