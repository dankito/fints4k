package net.dankito.fints.response.segments

import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung


open class CommunicationInfo(
    val bankInfo: Kreditinstitutskennung,
    val defaultLanguage: Dialogsprache,
    val parameters: List<CommunicationParameter>,
    segmentString: String
)
    : ReceivedSegment(segmentString)