package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


open class PinOrTan(pinOrTan: String?, existenzstatus: Existenzstatus)
    : AlphanumerischesDatenelement(pinOrTan, existenzstatus, 99)