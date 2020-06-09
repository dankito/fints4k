package net.dankito.banking.fints.messages.datenelemente.basisformate

import net.dankito.banking.fints.messages.Existenzstatus


/**
 * Es gilt der FinTS-Basiszeichensatz ohne die Zeichen CR und LF.
 */
open class AlphanumerischesDatenelement(
    alphanumericValue: String?, existenzstatus: Existenzstatus, val maxLength: Int? = null
) : TextDatenelement(alphanumericValue, existenzstatus) {


    override fun validate() {
        super.validate()

        if (writeToOutput) {
            value?.let { value -> // if value is null and value has to be written to output then validation already fails above
                if (value.contains("\r") || value.contains("\n")) {
                    throwValidationException("Alphanumerischer Wert '$value' darf kein Carriage Return (\r) oder " +
                            "Line Feed (\n) enthalten.")
                }

                maxLength?.let {
                    if (value.length > maxLength) {
                        throwValidationException("Wert '$value' darf maximal $maxLength Zeichen lang sein, " +
                                "hat aber ${value.length} Zeichen.")
                    }
                }
            }
        }
    }

}