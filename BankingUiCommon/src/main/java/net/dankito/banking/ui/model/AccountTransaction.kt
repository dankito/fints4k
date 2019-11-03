package net.dankito.banking.ui.model

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
    val bankAccount: BankAccount
) {

    // for object deserializers
    internal constructor() : this(0.toBigDecimal(),"", "", Date(), null, null, null, null, BankAccount())


    override fun toString(): String {
        return "${DateFormat.getDateInstance(DateFormat.MEDIUM).format(bookingDate)} $amount $otherPartyName: $usage"
    }

}