package net.dankito.banking.fints.messages.datenelemente.basisformate

import net.dankito.banking.fints.messages.Existenzstatus


/**
 * Zulässig sind die Ziffern ‘0’ bis ‘9’. Führende Nullen sind zugelassen.
 */
abstract class ZiffernDatenelement(value: Int?, numberOfDigits: Int, existenzstatus: Existenzstatus)
    : NumerischesDatenelement(value, numberOfDigits, existenzstatus) {


    @OptIn(ExperimentalStdlibApi::class)
    override fun formatValue(value: String): String {
        val formatted = StringBuilder("" + number)

        while (formatted.length < numberOfDigits) {
            formatted.insert(0, '0')
        }

        return formatted.toString()
    }

}