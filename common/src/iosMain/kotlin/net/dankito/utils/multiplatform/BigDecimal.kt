package net.dankito.utils.multiplatform

import platform.Foundation.*


fun NSDecimalNumber.toBigDecimal(): BigDecimal {
    return BigDecimal(this.stringValue) // TODO: find a better way than double string conversion
}

actual fun Collection<BigDecimal>.sum(): BigDecimal {
    return this.fold(NSDecimalNumber.zero) { acc, e -> acc.decimalNumberByAdding(e.decimal) }.toBigDecimal()
}


actual class BigDecimal(val decimal: NSDecimalNumber) : Comparable<BigDecimal> { // it's almost impossible to derive from NSDecimalNumber so i keep it as property

    actual companion object {
        actual val Zero = BigDecimal(0.0)
    }

    actual constructor(double: Double) : this(NSDecimalNumber(double))

    actual constructor(decimal: String) : this(decimal.toDouble())


    actual val isPositive: Boolean
        get() = this >= Zero


    actual fun negated(): BigDecimal {
        val negated = decimal.decimalNumberByMultiplyingBy(NSDecimalNumber(1.toULong(), 0, true))

        return BigDecimal(negated)
    }

    actual fun format(countDecimalPlaces: Int): String {
        val formatter = NSNumberFormatter()

        formatter.minimumFractionDigits = countDecimalPlaces.toULong()
        formatter.maximumFractionDigits = countDecimalPlaces.toULong()

        return formatter.stringFromNumber(this.decimal) ?: ""
    }


    override fun compareTo(other: BigDecimal): Int {
        return decimal.compare(other.decimal).toCompareToResult()
    }

    override fun equals(other: Any?): Boolean {
        if (other is BigDecimal) {
            return this.compareTo(other) == 0
        }

        return super.equals(other)
    }

    override fun hashCode(): Int {
        return decimal.hashCode()
    }


    override fun toString(): String {
        return decimal.description ?: decimal.descriptionWithLocale(NSLocale.currentLocale)
    }

}