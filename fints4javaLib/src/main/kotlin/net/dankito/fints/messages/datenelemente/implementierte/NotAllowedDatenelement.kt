package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.TextDatenelement


open class NotAllowedDatenelement : TextDatenelement("", Existenzstatus.NotAllowed)