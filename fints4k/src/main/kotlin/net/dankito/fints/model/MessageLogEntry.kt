package net.dankito.fints.model

import java.util.*


open class MessageLogEntry(
    val message: String,
    val time: Date,
    val customer: CustomerData
) {

    override fun toString(): String {
        return message
    }

}