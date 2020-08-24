package net.dankito.utils.multiplatform

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import net.dankito.utils.multiplatform.serialization.BigDecimalDeserializer


fun java.math.BigDecimal.toBigDecimal(): BigDecimal {
    return BigDecimal(this.toPlainString()) // TODO: find a better way than double string conversion
}

actual fun Collection<BigDecimal>.sum(): BigDecimal {
    return this.fold(java.math.BigDecimal.ZERO) { acc, e -> (acc + e) }.toBigDecimal()
}


@JsonDeserialize(using = BigDecimalDeserializer::class)
actual class BigDecimal actual constructor(decimal: String) : java.math.BigDecimal(decimal) {

    actual companion object {
        actual val Zero = BigDecimal(0.0)
    }


    internal constructor() : this("0") // for object deserializers

    actual constructor(double: Double) : this(java.math.BigDecimal.valueOf(double).toPlainString()) // for object deserializers


    actual val isPositive: Boolean
        get() = this >= ZERO

    actual fun format(countDecimalPlaces: Int): String {
        return String.format("%.0${countDecimalPlaces}f", this)
    }


    override fun equals(other: Any?): Boolean {
        if (other is BigDecimal) {
            return this.compareTo(other) == 0
        }

        return super.equals(other)
    }


    /*      TODO: where are these methods coming from?      */

    override fun toByte(): Byte {
        return 0 // will never be called; where is this method coming from?
    }

    override fun toChar(): Char {
        return 0.toChar() // will never be called; where is this method coming from?
    }

    override fun toShort(): Short {
        return 0 // will never be called; where is this method coming from?
    }

}