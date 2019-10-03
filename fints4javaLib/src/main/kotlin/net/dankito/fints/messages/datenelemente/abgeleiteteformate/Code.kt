package net.dankito.fints.messages.datenelemente.abgeleiteteformate

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Es sind nur die jeweils aufgeführten Werte zulässig.
 */
abstract class Code(code: String, val allowedValues: List<String>, existenzstatus: Existenzstatus)
    : AlphanumerischesDatenelement(code, existenzstatus) {

    override fun validate() {
        super.validate()

        if (allowedValues.contains(text) == false) {
            throwValidationException("'$text' ist kein Wert aus der Liste der zulässigen Werte: " +
                    allowedValues.joinToString(", ")
            )
        }
    }

}