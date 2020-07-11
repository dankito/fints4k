package net.dankito.utils.multiplatform

import platform.Foundation.NSCoder
import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSOrderedSame


fun NSDecimalNumber.toBigDecimal(): BigDecimal {
    return BigDecimal(this.stringValue) // TODO: find a better way than double string conversion
}

actual fun Collection<BigDecimal>.sum(): BigDecimal {
    return this.fold(NSDecimalNumber.zero) { acc, e -> acc.decimalNumberByAdding(e) }.toBigDecimal()
}


actual class BigDecimal actual constructor(decimal: String) : NSDecimalNumber(NSCoder()) {

    actual companion object {
        actual val Zero = BigDecimal("0") // TODO: is this correct?
    }


    actual constructor(double: Double) : this(NSDecimalNumber(double).stringValue)


    init {
        super.decimalNumberByAdding(NSDecimalNumber(decimal))
    }


    actual fun format(pattern: String): String {
        val formatter = NSNumberFormatter()

        formatter.positiveFormat = pattern
        formatter.negativeFormat = pattern

        return formatter.stringFromNumber(this) ?: ""
    }


    override fun isEqual(`object`: Any?): Boolean {
        if (`object` is BigDecimal) {
            return this.compare(`object`) == NSOrderedSame
        }

        return super.equals(`object`)
    }

}