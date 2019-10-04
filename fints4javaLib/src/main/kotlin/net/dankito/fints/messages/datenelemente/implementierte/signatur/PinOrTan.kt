package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


open class PinOrTan(pinOrTan: String) : AlphanumerischesDatenelement(pinOrTan, Existenzstatus.Mandatory)