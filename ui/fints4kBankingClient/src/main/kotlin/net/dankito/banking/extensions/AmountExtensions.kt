package net.dankito.banking.extensions

import net.dankito.banking.fints.model.Amount
import net.dankito.banking.fints.model.Money
import java.math.BigDecimal


fun BigDecimal.toAmount(): Amount {
    return Amount(this.toString())
}

fun BigDecimal.toMoney(): Money {
    return Money(this.toAmount(), "EUR")
}

fun Amount.toJavaBigDecimal(): BigDecimal {
    return BigDecimal(this.string.replace(',', '.'))
}

fun Money.toJavaBigDecimal(): BigDecimal {
    return this.amount.toJavaBigDecimal()
}