package net.dankito.banking.fints.response.segments

import net.dankito.banking.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung


open class CommunicationInfo(
    val bankInfo: Kreditinstitutskennung,
    val defaultLanguage: Dialogsprache,
    val parameters: List<CommunicationParameter>,
    segmentString: String
)
    : ReceivedSegment(segmentString)