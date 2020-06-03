package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Versionsnummer des entsprechenden Schlüssels.
 */
open class Schluesselversion(version: Int) : NumerischesDatenelement(version, 3, Existenzstatus.Mandatory) {

    companion object {
        const val FinTsMockValue = 0
    }

}