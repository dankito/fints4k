package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


open class PinOrTan(pinOrTan: String?, existenzstatus: Existenzstatus)
    : AlphanumerischesDatenelement(pinOrTan, existenzstatus, 99)