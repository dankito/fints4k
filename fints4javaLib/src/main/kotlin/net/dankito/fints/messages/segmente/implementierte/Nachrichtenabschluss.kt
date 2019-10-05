package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.Nachrichtennummer
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.model.DialogData


/**
 * Dieses Segment beendet alle Kunden- und Kreditinstitutsnachrichten.
 */
open class Nachrichtenabschluss(
    segmentNumber: Int,
    dialogData: DialogData

) : Segment(listOf(
        Segmentkopf("HNHBS", 1, segmentNumber),
        Nachrichtennummer(dialogData.messageNumber)
), Existenzstatus.Mandatory)