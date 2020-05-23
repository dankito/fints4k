package net.dankito.banking.fints.model

import java.math.BigDecimal
import java.text.DateFormat
import java.util.*


open class AccountTransaction(
    val account: AccountData,
    val amount: BigDecimal,
    val currency: String,
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

    // for object deserializers
    internal constructor() : this(AccountData(), 0.toBigDecimal(),"", false, "", Date(), null, null, null, null, Date(), 0, null, null, null,
    null, null, null, null, null, null, null, null, null, null, null,  null, null,
    null, "", "", null, null, "", null)


    override fun toString(): String {
        return "${DateFormat.getDateInstance(DateFormat.MEDIUM).format(bookingDate)} $amount $otherPartyName: $unparsedUsage"
    }

}