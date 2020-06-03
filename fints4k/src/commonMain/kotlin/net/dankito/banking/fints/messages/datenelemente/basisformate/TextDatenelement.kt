package net.dankito.banking.fints.messages.datenelemente.basisformate

import io.ktor.utils.io.charsets.encode
import io.ktor.utils.io.charsets.name
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
                value?.let { // at this time value is != null otherwise checkIfMandatoryValueIsSet() would fail
                    if (HbciCharset.DefaultCharset.newEncoder().encode(it).canRead() == false) {
                        throwInvalidCharacterException()
                    }
                }
            } catch (e: Exception) {
                throwInvalidCharacterException()
            }
        }
    }

    protected open fun checkIfMandatoryValueIsSet() {
        if (existenzstatus == Existenzstatus.Mandatory && value == null) {
            throwValidationException("Wert ist auf dem Pflichtfeld ${this::class.simpleName} not set")
        }
    }

    protected open fun throwInvalidCharacterException() {
        throwValidationException(
            "Wert '$value' enthält Zeichen die gemäß des Zeichensatzes " +
                    "${HbciCharset.DefaultCharset.name} nicht erlaubt sind."
        )
    }

}