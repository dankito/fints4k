package net.dankito.fints.response.segments

import java.math.BigDecimal
import java.util.*


open class BalanceSegment(
    val balance: BigDecimal,
    val currency: String,
    val date: Date,
    val accountProductName: String,
    val balanceOfPreBookedTransactions: BigDecimal?,
    segmentString: String
)
    : ReceivedSegment(segmentString)