package net.dankito.fints.messages.datenelemente.basisformate

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.Datenelement


/**
 * Zulässig sind lediglich die Ziffern ‘0’ bis ‘9’. Führende Nullen sind nicht zugelassen.
 */
abstract class NumerischesDatenelement(val value: Int, val numberOfDigits: Int, existenzstatus: Existenzstatus)
    : Datenelement(existenzstatus) {


    override fun format(): String {
        return value.toString()
    }


    override fun validate() {
        val maxValue = Math.pow(10.0, numberOfDigits.toDouble()) - 1

        if (value < 0 || value > maxValue) {
            throwValidationException("Wert '$value' muss im Wertebereich von 0 - $maxValue liegen.")
        }
    }

}