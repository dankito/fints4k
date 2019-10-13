package net.dankito.fints.messages.datenelemente.basisformate

import net.dankito.fints.messages.Existenzstatus


/**
 * Zulässig sind lediglich die Ziffern ‘0’ bis ‘9’. Führende Nullen sind nicht zugelassen.
 */
abstract class NumerischesDatenelement(val number: Int?, val numberOfDigits: Int, existenzstatus: Existenzstatus)
    : TextDatenelement(number?.toString(), existenzstatus) {


    override fun validate() {
        super.validate()

        if (writeToOutput && number != null) { // if number is null and number has to be written to output then validation already fails above
            val maxValue = Math.pow(10.0, numberOfDigits.toDouble()) - 1

            if (number < 0 || number > maxValue) {
                throwValidationException("Wert '$number' muss im Wertebereich von 0 - $maxValue liegen.")
            }
        }
    }

}