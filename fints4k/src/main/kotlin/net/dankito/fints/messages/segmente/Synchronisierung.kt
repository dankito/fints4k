package net.dankito.fints.messages.segmente

import net.dankito.fints.messages.datenelemente.implementierte.Synchronisierungsmodus
import net.dankito.fints.messages.datenelemente.implementierte.SynchronisierungsmodusDatenelement
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.id.CustomerSegmentId


open class Synchronisierung(
    segmentNumber: Int,
    mode: Synchronisierungsmodus

) : Segment(listOf(
    Segmentkopf(CustomerSegmentId.Synchronization, 3, segmentNumber),
    SynchronisierungsmodusDatenelement(mode)
))