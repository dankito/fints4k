package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus


open class BezugssegmentInKundennachricht(segmentNumber: Int) : Bezugssegment(segmentNumber, Existenzstatus.NotAllowed)