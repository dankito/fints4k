package net.dankito.banking.fints.messages.datenelemente.basisformate

import net.dankito.banking.fints.messages.Existenzstatus


/**
 * Zulässig sind die Ziffern ‘0’ bis ‘9’. Führende Nullen sind zugelassen.
 */
abstract class ZiffernDatenelement(value: Int?, numberOfDigits: Int, existenzstatus: Existenzstatus)
    : NumerischesDatenelement(value, numberOfDigits, existenzstatus) {


    override fun formatValue(value: String): String {
        return String.format("%0${numberOfDigits}d", number)
    }

}