package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.TextDatenelement


open class NotAllowedDatenelement : TextDatenelement("", Existenzstatus.NotAllowed)