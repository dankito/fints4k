package net.dankito.banking.fints.model


open class Money(
    val amount: Amount,
    val currency: Currency
) {


    constructor(amount: Amount, currencyCode: String) : this(amount, Currency(currencyCode))

    internal constructor() : this(Amount.Zero, "") // for object deserializers


    open val displayString: String
        get() = "$amount $currency"


    override fun toString(): String {
        return displayString
    }

}