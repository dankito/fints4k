package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Schlüsselnummer des entsprechenden Schlüssels.
 */
open class Schluesselnummer(number: Int) : NumerischesDatenelement(number, 3, Existenzstatus.Mandatory) {

    companion object {
        const val FinTsMockValue = 0
    }

}