package net.dankito.banking.extensions

import net.dankito.banking.fints.model.Amount
import net.dankito.banking.fints.model.Money
import net.dankito.utils.multiplatform.BigDecimal


fun BigDecimal.toAmount(): Amount {
    return Amount(this.toString())
}

fun BigDecimal.toMoney(): Money {
    return Money(this.toAmount(), "EUR")
}