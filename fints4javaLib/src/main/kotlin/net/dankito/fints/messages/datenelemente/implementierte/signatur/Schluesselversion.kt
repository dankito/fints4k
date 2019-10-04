package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Versionsnummer des entsprechenden Schlüssels.
 */
open class Schluesselversion(version: Int) : NumerischesDatenelement(version, 3, Existenzstatus.Mandatory) {

    companion object {
        const val FinTsMockValue = 0
    }

}