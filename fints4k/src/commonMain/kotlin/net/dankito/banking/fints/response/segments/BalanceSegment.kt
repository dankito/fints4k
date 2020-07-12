package net.dankito.banking.fints.response.segments

import net.dankito.banking.fints.model.Amount
import net.dankito.utils.multiplatform.Date


open class BalanceSegment(
    val balance: Amount,
    val currency: String,
    val date: Date,
    val accountProductName: String,
    val balanceOfPreBookedTransactions: Amount?,
    segmentString: String
)
    : ReceivedSegment(segmentString)