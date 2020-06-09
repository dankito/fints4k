package net.dankito.banking.fints.messages.datenelemente.basisformate

import net.dankito.banking.fints.messages.Existenzstatus


/**
 * Zulässig sind lediglich die Ziffern ‘0’ bis ‘9’. Führende Nullen sind nicht zugelassen.
 */
open class NumerischesDatenelement(val number: Int?, val numberOfDigits: Int, existenzstatus: Existenzstatus)
    : TextDatenelement(number?.toString(), existenzstatus) {


    override fun validate() {
        super.validate()

        if (writeToOutput && number != null) { // if number is null and number has to be written to output then validation already fails above
            // can't believe it, there's no Math.pow() in Kotlin multiplatform
            var maxValue = 1
            IntRange(1, numberOfDigits).forEach {
                maxValue *= 10
            }
            maxValue -= 1

            if (number < 0 || number > maxValue) {
                throwValidationException("Wert '$number' muss im Wertebereich von 0 - $maxValue liegen.")
            }
        }
    }

}