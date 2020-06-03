package net.dankito.banking.fints.messages.segmente.implementierte

import net.dankito.banking.fints.messages.datenelemente.implementierte.Nachrichtennummer
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.banking.fints.messages.segmente.Segment
import net.dankito.banking.fints.messages.segmente.id.MessageSegmentId
import net.dankito.banking.fints.model.DialogContext


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