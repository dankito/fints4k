package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.TextDatenelement


/**
 * A dummy data element for conditional data elements building to tell formatter not to print this data element
 */
open class DoNotPrintDatenelement : TextDatenelement("", Existenzstatus.NotAllowed)