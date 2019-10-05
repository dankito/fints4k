package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.DialogId
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.SegmentId
import net.dankito.fints.model.DialogData


class Dialogende(
    segmentNumber: Int,
    dialogData: DialogData

) : Segment(listOf(
        Segmentkopf(SegmentId.DialogEnd, 1, segmentNumber),
        DialogId(dialogData.dialogId)
), Existenzstatus.Mandatory)