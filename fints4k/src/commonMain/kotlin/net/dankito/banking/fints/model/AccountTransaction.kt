package net.dankito.banking.fints.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.soywiz.klock.Date
import com.soywiz.klock.DateTime


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
    internal constructor() : this(AccountData(), Money(BigDecimal.ZERO, ""), false, "", DateTime.EPOCH.date, null, null, null, null, DateTime.EPOCH.date, 0, null, null, null,
    null, null, null, null, null, null, null, null, null, null, null,  null, null,
    null, "", "", null, null, "", null)


    override fun toString(): String {
        return "$valueDate $amount $otherPartyName: $unparsedUsage"
    }

}