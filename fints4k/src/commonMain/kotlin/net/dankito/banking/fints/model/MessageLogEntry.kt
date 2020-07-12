package net.dankito.banking.fints.model

import net.dankito.utils.multiplatform.Date


open class MessageLogEntry(
    val message: String,
    val time: Date,
    val customer: CustomerData
) {

    override fun toString(): String {
        return message
    }

}