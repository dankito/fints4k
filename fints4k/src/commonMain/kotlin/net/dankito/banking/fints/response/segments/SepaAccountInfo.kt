package net.dankito.banking.fints.response.segments


open class SepaAccountInfo(
    val account: KontoverbindungZvInternational,
    segmentString: String
)
    : ReceivedSegment(segmentString)