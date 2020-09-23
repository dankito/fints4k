package net.dankito.banking.fints.model

import net.dankito.utils.multiplatform.Date


open class CreditCardTransaction(
    val amount: Money,
    val otherPartyName: String,
    val bookingDate: Date,
    val valueDate: Date,
    val isCleared: Boolean
)