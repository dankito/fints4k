package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Schlüsselnummer des entsprechenden Schlüssels.
 */
open class Schluesselnummer(number: Int) : NumerischesDatenelement(number, 3, Existenzstatus.Mandatory) {

    companion object {
        const val PinTanDefaultValue = 0
    }

}