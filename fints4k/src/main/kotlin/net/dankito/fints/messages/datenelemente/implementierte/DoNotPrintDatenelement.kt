package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.TextDatenelement


/**
 * A dummy data element for conditional data elements building to tell formatter not to print this data element
 */
open class DoNotPrintDatenelement : TextDatenelement("", Existenzstatus.NotAllowed)