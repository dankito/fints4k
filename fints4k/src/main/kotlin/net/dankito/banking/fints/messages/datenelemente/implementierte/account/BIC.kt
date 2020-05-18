package net.dankito.banking.fints.messages.datenelemente.implementierte.account

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


open class BIC(bic: String, existenzstatus: Existenzstatus) : AlphanumerischesDatenelement(bic, existenzstatus, 11)