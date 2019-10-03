package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.Nachrichtennummer
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment


/**
 * Dieses Segment beendet alle Kunden- und Kreditinstitutsnachrichten.
 */
class Nachrichtenabschluss(
    segmentNumber: Int,
    messageNumber: Int
) : Segment(listOf(
        Segmentkopf("HNHBS", segmentNumber, 1),
        Nachrichtennummer(messageNumber)
), Existenzstatus.Mandatory)