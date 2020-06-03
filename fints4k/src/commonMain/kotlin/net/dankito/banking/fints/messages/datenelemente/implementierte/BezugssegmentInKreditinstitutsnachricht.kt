package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus


open class BezugssegmentInKreditinstitutsnachricht(segmentNumber: Int) : Bezugssegment(segmentNumber, Existenzstatus.Optional)