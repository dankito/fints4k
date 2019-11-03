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


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountTransaction) return false

        if (amount.compareTo(other.amount) != 0) return false
        if (currency != other.currency) return false
        if (usage != other.usage) return false
        if (bookingDate != other.bookingDate) return false
        if (otherPartyName != other.otherPartyName) return false
        if (otherPartyBankCode != other.otherPartyBankCode) return false
        if (otherPartyAccountId != other.otherPartyAccountId) return false
        if (bookingText != other.bookingText) return false
        if (bankAccount != other.bankAccount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = amount.hashCode()
        result = 31 * result + currency.hashCode()
        result = 31 * result + usage.hashCode()
        result = 31 * result + bookingDate.hashCode()
        result = 31 * result + (otherPartyName?.hashCode() ?: 0)
        result = 31 * result + (otherPartyBankCode?.hashCode() ?: 0)
        result = 31 * result + (otherPartyAccountId?.hashCode() ?: 0)
        result = 31 * result + (bookingText?.hashCode() ?: 0)
        result = 31 * result + bankAccount.hashCode()
        return result
    }


    override fun toString(): String {
        return "${DateFormat.getDateInstance(DateFormat.MEDIUM).format(bookingDate)} $amount $otherPartyName: $usage"
    }

}