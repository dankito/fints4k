package net.dankito.fints.response.segments

import java.math.BigDecimal
import java.util.*


open class Balance(
    val amount: BigDecimal,
    val date: Date,
    val time: Date?
) {

    override fun toString(): String {
        return "$amount ($date)"
    }

}