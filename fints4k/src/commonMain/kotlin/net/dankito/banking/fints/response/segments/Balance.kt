package net.dankito.banking.fints.response.segments

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.soywiz.klock.Date
import com.soywiz.klock.Time


open class Balance(
    val amount: BigDecimal,
    val date: Date,
    val time: Time?
) {

    override fun toString(): String {
        return "$amount ($date)"
    }

}