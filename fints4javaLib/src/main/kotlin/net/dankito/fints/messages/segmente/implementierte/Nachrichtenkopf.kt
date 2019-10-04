package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.*
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment


open class Nachrichtenkopf(
    segmentNumber: Int,
    messageSize: Int,
    dialogId: String,
    messageNumber: Int

) : Segment(listOf(
        Segmentkopf("HNHBK", 3, segmentNumber),
        Nachrichtengroesse(messageSize),
        HbciVersionDatenelement(HbciVersion.FinTs_3_0_0),
        DialogId(dialogId),
        Nachrichtennummer(messageNumber)
), Existenzstatus.Mandatory)