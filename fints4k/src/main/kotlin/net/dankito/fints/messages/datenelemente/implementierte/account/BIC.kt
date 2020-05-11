package net.dankito.fints.messages.datenelemente.implementierte.account

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


open class BIC(bic: String, existenzstatus: Existenzstatus) : AlphanumerischesDatenelement(bic, existenzstatus, 11)