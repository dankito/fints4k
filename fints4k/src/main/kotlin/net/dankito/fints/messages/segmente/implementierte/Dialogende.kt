package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.DialogId
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.fints.model.DialogContext


class Dialogende(
    segmentNumber: Int,
    dialogContext: DialogContext

) : Segment(listOf(
        Segmentkopf(CustomerSegmentId.DialogEnd, 1, segmentNumber),
        DialogId(dialogContext.dialogId)
))