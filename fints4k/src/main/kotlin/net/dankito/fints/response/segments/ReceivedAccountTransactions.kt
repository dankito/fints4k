package net.dankito.fints.response.segments


open class ReceivedAccountTransactions(
    val bookedTransactionsString: String,
    val unbookedTransactionsString: String?, // TODO
    segmentString: String

)
    : ReceivedSegment(segmentString)