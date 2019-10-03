package net.dankito.fints.messages.datenelemente.basisformate

import net.dankito.fints.messages.Existenzstatus


/**
 * Zulässig sind die Ziffern ‘0’ bis ‘9’. Führende Nullen sind zugelassen.
 */
abstract class ZiffernDatenelement(value: Int, numberOfDigits: Int, existenzstatus: Existenzstatus)
    : NumerischesDatenelement(value, numberOfDigits, existenzstatus) {


    override fun format(): String {
        return String.format("%0${numberOfDigits}d", value)
    }

}