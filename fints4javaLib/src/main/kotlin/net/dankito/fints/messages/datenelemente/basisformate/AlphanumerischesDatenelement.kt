package net.dankito.fints.messages.datenelemente.basisformate

import net.dankito.fints.messages.Existenzstatus


/**
 * Es gilt der FinTS-Basiszeichensatz ohne die Zeichen CR und LF.
 */
abstract class AlphanumerischesDatenelement @JvmOverloads constructor(
    alphanumericValue: String, existenzstatus: Existenzstatus, val maxLength: Int? = null
) : TextDatenelement(alphanumericValue, existenzstatus) {


    override fun validate() {
        super.validate()

        if (text.contains("\r") || text.contains("\n")) {
            throwValidationException("Alphanumerischer Wert '$text' darf kein Carriage Return (\r) oder " +
                    "Line Feed (\n) enthalten.")
        }

        maxLength?.let {
            if (text.length > maxLength) {
                throwValidationException("Wert '$text' darf maximal $maxLength Zeichen lang sein, " +
                        "hat aber ${text.length} Zeichen.")
            }
        }
    }

}