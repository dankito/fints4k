package net.dankito.fints.messages.datenelemente.basisformate

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.HbciCharset
import net.dankito.fints.messages.datenelemente.Datenelement


/**
 * Es gilt der vollständige FinTS-Basiszeichensatz.
 */
abstract class TextDatenelement(val value: String?, existenzstatus: Existenzstatus) : Datenelement(existenzstatus) {


    override val isValueSet = value != null

    override fun format(): String {
        if (writeToOutput) {
            value?.let {
                return formatValue(it)
            }
        }

        return ""
    }

    protected open fun formatValue(value: String): String {
        return value // may overwritten in sub classes
    }


    override fun validate() {
        if (writeToOutput) {
            checkIfMandatoryValueIsSet()

            try {
                if (HbciCharset.DefaultCharset.newEncoder().canEncode(value) == false) {
                    throwInvalidCharacterException()
                }
            } catch (e: Exception) {
                throwInvalidCharacterException()
            }
        }
    }

    protected open fun checkIfMandatoryValueIsSet() {
        if (existenzstatus == Existenzstatus.Mandatory && value == null) {
            throwValidationException("Wert ist auf dem Pflichtfeld ${javaClass.simpleName} not set")
        }
    }

    protected open fun throwInvalidCharacterException() {
        throwValidationException(
            "Wert '$value' enthält Zeichen die gemäß des Zeichensatzes " +
                    "${HbciCharset.DefaultCharset.displayName()} nicht erlaubt sind."
        )
    }

}