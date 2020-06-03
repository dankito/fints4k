package net.dankito.banking.fints.response.segments

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.soywiz.klock.Date


open class BalanceSegment(
    val balance: BigDecimal,
    val currency: String,
    val date: Date,
    val accountProductName: String,
    val balanceOfPreBookedTransactions: BigDecimal?,
    segmentString: String
)
    : ReceivedSegment(segmentString)