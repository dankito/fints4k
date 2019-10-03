package net.dankito.fints.messages.datenelemente.basisformate

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.HbciCharset
import net.dankito.fints.messages.datenelemente.Datenelement


/**
 * Es gilt der vollständige FinTS-Basiszeichensatz.
 */
abstract class TextDatenelement(val text: String, existenzstatus: Existenzstatus) : Datenelement(existenzstatus) {


    override fun format(): String {
        return text
    }


    override fun validate() {
        try {
            if (HbciCharset.DefaultCharset.newEncoder().canEncode(text) == false) {
                throwInvalidCharacterException()
            }
        } catch (e: Exception) {
            throwInvalidCharacterException()
        }
    }

    protected open fun throwInvalidCharacterException() {
        throwValidationException(
            "Wert '$text' enthält Zeichen die gemäß des Zeichensatzes " +
                    "${HbciCharset.DefaultCharset.displayName()} nicht erlaubt sind."
        )
    }

}