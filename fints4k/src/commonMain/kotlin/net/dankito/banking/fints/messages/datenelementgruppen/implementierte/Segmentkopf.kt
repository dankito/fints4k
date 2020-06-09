package net.dankito.banking.fints.messages.datenelementgruppen.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.implementierte.Segmentkennung
import net.dankito.banking.fints.messages.datenelemente.implementierte.Segmentnummer
import net.dankito.banking.fints.messages.datenelemente.implementierte.Segmentversion
import net.dankito.banking.fints.messages.datenelementgruppen.Datenelementgruppe
import net.dankito.banking.fints.messages.segmente.id.ISegmentId


open class Segmentkopf(
    identifier: String,
    segmentVersion: Int,
    segmentNumber: Int = 0,
    bezugssegment: Int? = null

) : Datenelementgruppe(listOf(
    Segmentkennung(identifier),
    Segmentnummer(segmentNumber),
    Segmentversion(segmentVersion) // TODO: how to conditionally add Bezugsegment?
), Existenzstatus.Mandatory) {

    constructor(id: ISegmentId, segmentVersion: Int, segmentNumber: Int) : this(id.id, segmentVersion, segmentNumber)

}