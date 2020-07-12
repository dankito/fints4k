package net.dankito.banking.fints.response.segments

import net.dankito.banking.fints.model.Amount
import net.dankito.utils.multiplatform.Date


open class Balance(
    val amount: Amount,
    val date: Date,
    val time: Date?
) {

    override fun toString(): String {
        return "$amount ($date)"
    }

}