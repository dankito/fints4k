package net.dankito.banking.extensions

import com.ionspin.kotlin.bignum.integer.Sign
import net.dankito.banking.fints.model.Money
import java.math.BigDecimal


fun BigDecimal.toIonspinBigDecimal(): com.ionspin.kotlin.bignum.decimal.BigDecimal {
    return com.ionspin.kotlin.bignum.decimal.BigDecimal.parseString(this.toString()) // TODO: couldn't figure out how to create BigDecimal from unscaledValue and scale
}

fun com.ionspin.kotlin.bignum.decimal.BigDecimal.toJavaBigDecimal(): BigDecimal {
    val converted = BigDecimal.valueOf(this.significand.longValue(), (this.precision - this.exponent - 1).toInt())

    if (this.significand.sign == Sign.NEGATIVE) {
        return converted.negate()
    }

    return converted
}

fun Money.toJavaBigDecimal(): BigDecimal {
    return this.amount.toJavaBigDecimal()
}