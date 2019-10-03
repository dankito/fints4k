package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus


open class BezugssegmentInKundennachricht(segmentNumber: Int) : Bezugssegment(segmentNumber, Existenzstatus.NotAllowed)