package net.dankito.banking.fints.messages.datenelemente.basisformate

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.HbciCharset
import net.dankito.banking.fints.messages.Separators
import net.dankito.banking.fints.messages.datenelemente.Datenelement


/**
 * Es gilt der vollständige FinTS-Basiszeichensatz.
 */
abstract class TextDatenelement(var value: String?, existenzstatus: Existenzstatus) : Datenelement(existenzstatus) {


    override val isValueSet
        get() = value != null

    override fun format(): String {
        if (writeToOutput) {
            value?.let {
                return formatValue(it)
            }
        }

        return ""
    }

    protected open fun formatValue(value: String): String {
        return maskMessagePart(value, Separators.AllSeparators) // may overwritten in sub classes
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