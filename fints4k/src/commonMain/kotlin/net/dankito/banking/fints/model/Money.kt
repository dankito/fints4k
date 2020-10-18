package net.dankito.banking.fints.model

import net.dankito.utils.multiplatform.BigDecimal


open class Money(
    val amount: Amount,
    val currency: Currency
) {


    constructor(amount: Amount, currencyCode: String) : this(amount, Currency(currencyCode))

    internal constructor() : this(Amount.Zero, "") // for object deserializers



    open val bigDecimal: BigDecimal
        get() = amount.bigDecimal

    open val displayString: String
        get() = "$amount $currency"



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Money) return false

        if (amount != other.amount) return false
        if (currency != other.currency) return false

        return true
    }

    override fun hashCode(): Int {
        var result = amount.hashCode()
        result = 31 * result + currency.hashCode()
        return result
    }


    override fun toString(): String {
        return displayString
    }

}