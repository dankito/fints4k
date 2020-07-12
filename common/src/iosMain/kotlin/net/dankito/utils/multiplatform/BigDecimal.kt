package net.dankito.utils.multiplatform

import platform.Foundation.*


fun NSDecimalNumber.toBigDecimal(): BigDecimal {
    return BigDecimal(this.stringValue) // TODO: find a better way than double string conversion
}

actual fun Collection<BigDecimal>.sum(): BigDecimal {
    return this.fold(NSDecimalNumber.zero) { acc, e -> acc.decimalNumberByAdding(e.decimal) }.toBigDecimal()
}


actual class BigDecimal(val decimal: NSDecimalNumber) { // it's almost impossible to derive from NSDecimalNumber so i keep it as property

    actual companion object {
        actual val Zero = BigDecimal(0.0)
    }

    actual constructor(double: Double) : this(NSDecimalNumber(double))

    actual constructor(decimal: String) : this(decimal.toDouble())


    actual fun format(pattern: String): String {
        val formatter = NSNumberFormatter()

        formatter.positiveFormat = pattern
        formatter.negativeFormat = pattern

        return formatter.stringFromNumber(this.decimal) ?: ""
    }


    override fun equals(other: Any?): Boolean {
        if (other is BigDecimal) {
            return this.decimal.compare(other.decimal) == NSOrderedSame
        }

        return super.equals(other)
    }

}