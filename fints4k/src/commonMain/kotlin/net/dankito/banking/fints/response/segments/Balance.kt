package net.dankito.banking.fints.response.segments

import com.soywiz.klock.Date
import com.soywiz.klock.Time
import net.dankito.banking.fints.model.Amount


open class Balance(
    val amount: Amount,
    val date: Date,
    val time: Time?
) {

    override fun toString(): String {
        return "$amount ($date)"
    }

}