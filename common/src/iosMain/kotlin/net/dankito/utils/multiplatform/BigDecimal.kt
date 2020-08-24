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

    actual fun format(countDecimalPlaces: Int): String {
        val formatter = NSNumberFormatter()

        formatter.minimumFractionDigits = countDecimalPlaces.toULong()
        formatter.maximumFractionDigits = countDecimalPlaces.toULong()

        return formatter.stringFromNumber(this.decimal) ?: ""
    }


    override fun compareTo(other: BigDecimal): Int {
        return when (decimal.compare(other.decimal)) {
            NSOrderedSame -> 0
            NSOrderedAscending -> -1
            NSOrderedDescending -> 1
            else -> 0
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is BigDecimal) {
            return this.compareTo(other) == 0
        }

        return super.equals(other)
    }


    override fun toString(): String {
        return decimal.description ?: decimal.descriptionWithLocale(NSLocale.currentLocale)
    }

}