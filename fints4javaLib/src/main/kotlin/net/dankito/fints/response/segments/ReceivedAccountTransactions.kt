package net.dankito.fints.response.segments

import net.dankito.fints.model.AccountTransaction


open class ReceivedAccountTransactions(
    val bookedTransactions: List<AccountTransaction>,
    val unbookedTransactions: List<Any>, // TODO
    segmentString: String

)
    : ReceivedSegment(segmentString)