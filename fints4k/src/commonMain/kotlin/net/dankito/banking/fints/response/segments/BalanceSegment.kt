package net.dankito.banking.fints.response.segments

import com.soywiz.klock.Date
import net.dankito.banking.fints.model.Amount


open class BalanceSegment(
    val balance: Amount,
    val currency: String,
    val date: Date,
    val accountProductName: String,
    val balanceOfPreBookedTransactions: Amount?,
    segmentString: String
)
    : ReceivedSegment(segmentString)