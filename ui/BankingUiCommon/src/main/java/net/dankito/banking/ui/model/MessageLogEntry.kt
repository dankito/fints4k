package net.dankito.banking.ui.model

import java.util.*


open class MessageLogEntry(
    val message: String,
    val time: Date,
    val customer: Customer
) {

    override fun toString(): String {
        return message
    }

}