package net.dankito.banking.fints.model

import com.soywiz.klock.DateTime


open class MessageLogEntry(
    val message: String,
    val time: DateTime,
    val customer: CustomerData
) {

    override fun toString(): String {
        return message
    }

}