package net.dankito.banking.ui.util


open class CurrencyInfo(
    open val displayName: String,
    open val isoCode: String,
    open val numericCode: Int,
    open val symbol: String,
    open val defaultFractionDigits: Int
) {

    override fun toString(): String {
        return "$isoCode $displayName"
    }

}