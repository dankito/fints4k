package net.dankito.banking.ui.model

import net.dankito.utils.multiplatform.Date


open class MessageLogEntry(
    val message: String,
    val time: Date,
    val customer: Customer
) {

    override fun toString(): String {
        return message
    }

}