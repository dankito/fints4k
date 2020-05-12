package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.Nachrichtennummer
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.MessageSegmentId
import net.dankito.fints.model.DialogContext


/**
 * Dieses Segment beendet alle Kunden- und Kreditinstitutsnachrichten.
 */
open class Nachrichtenabschluss(
    segmentNumber: Int,
    dialogContext: DialogContext

) : Segment(listOf(
        Segmentkopf(MessageSegmentId.MessageEnding, 1, segmentNumber),
        Nachrichtennummer(dialogContext.messageNumber)
))