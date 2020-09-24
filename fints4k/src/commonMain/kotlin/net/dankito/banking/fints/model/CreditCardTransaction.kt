package net.dankito.banking.fints.model

import net.dankito.utils.multiplatform.Date
import net.dankito.utils.multiplatform.format


open class CreditCardTransaction(
    val amount: Money,
    val otherPartyName: String,
    val bookingDate: Date,
    val valueDate: Date,
    val isCleared: Boolean
) {

    override fun toString(): String {
        return "${valueDate.format("dd.MM.yy")} $amount $otherPartyName"
    }

}