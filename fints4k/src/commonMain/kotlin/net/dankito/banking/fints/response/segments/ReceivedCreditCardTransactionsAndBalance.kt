package net.dankito.banking.fints.response.segments

import net.dankito.banking.fints.model.CreditCardTransaction


open class ReceivedCreditCardTransactionsAndBalance(
    val balance: Balance,
    val transactions: List<CreditCardTransaction>,
    segmentString: String

) : ReceivedSegment(segmentString)