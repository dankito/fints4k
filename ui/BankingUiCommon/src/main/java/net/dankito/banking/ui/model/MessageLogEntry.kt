package net.dankito.banking.ui.model

import java.util.*


open class MessageLogEntry(
    val message: String,
    val time: Date,
    val account: Account
) {

    override fun toString(): String {
        return message
    }

}