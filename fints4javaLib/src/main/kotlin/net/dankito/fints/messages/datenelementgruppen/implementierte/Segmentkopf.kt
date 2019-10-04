package net.dankito.fints.messages.datenelementgruppen.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.Segmentkennung
import net.dankito.fints.messages.datenelemente.implementierte.Segmentnummer
import net.dankito.fints.messages.datenelemente.implementierte.Segmentversion
import net.dankito.fints.messages.datenelementgruppen.Datenelementgruppe


open class Segmentkopf @JvmOverloads constructor(
    identifier: String,
    segmentVersion: Int,
    segmentNumber: Int = 0,
    bezugssegment: Int? = null

) : Datenelementgruppe(listOf(
    Segmentkennung(identifier),
    Segmentnummer(segmentNumber),
    Segmentversion(segmentVersion) // TODO: how to conditionally add Bezugsegment?
), Existenzstatus.Mandatory) {
}