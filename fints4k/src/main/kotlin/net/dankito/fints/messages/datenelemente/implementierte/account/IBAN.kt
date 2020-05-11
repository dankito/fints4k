package net.dankito.fints.messages.datenelemente.implementierte.account

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


open class IBAN(iban: String, existenzstatus: Existenzstatus) : AlphanumerischesDatenelement(iban, existenzstatus, 34)