package net.dankito.banking.fints.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal


open class Money(
    val amount: BigDecimal,
    val currency: Currency
) {

    constructor(amount: BigDecimal, currencyCode: String) : this(amount, Currency(currencyCode))

    internal constructor() : this(BigDecimal.ZERO, "") // for object deserializers


    open val displayString: String
        get() = "$amount $currency"


    override fun toString(): String {
        return displayString
    }

}