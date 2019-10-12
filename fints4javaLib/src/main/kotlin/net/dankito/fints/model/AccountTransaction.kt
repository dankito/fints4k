package net.dankito.fints.model

import java.math.BigDecimal
import java.text.DateFormat
import java.util.*


open class AccountTransaction(
    val amount: BigDecimal,
    val currency: String,
    val usage: String,
    val bookingDate: Date,
    val otherPartyName: String?,
    val otherPartyBankCode: String?,
    val otherPartyAccountId: String?,
    val bookingText: String?,
    val valueDate: Date?,
    val openingBalance: BigDecimal?,
    val closingBalance: BigDecimal?
    // TODO: may also add other values from parsed usage lines
) {

    // for object deserializers
    private constructor() : this(0.toBigDecimal(),"", "", Date(), null, null, null, null, null, null, null)


    override fun toString(): String {
        return "${DateFormat.getDateInstance(DateFormat.MEDIUM).format(bookingDate)} $amount $otherPartyName: $usage"
    }

}