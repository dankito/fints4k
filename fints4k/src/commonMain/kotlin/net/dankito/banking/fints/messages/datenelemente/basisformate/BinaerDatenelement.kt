package net.dankito.banking.fints.messages.datenelemente.basisformate

import net.dankito.banking.fints.messages.Existenzstatus


/**
 * Binäre Daten werden unverändert in den FinTS-Datensatz eingestellt. Eine Umwandlung in
 * eine Zeichendarstellung erfolgt nicht. Es ist zu beachten, dass der FinTS-Basiszeichensatz
 * für binäre Daten keine Gültigkeit besitzt. Ferner gelten die speziellen Syntaxregeln für
 * binäre Daten (s. Kap. H.1.3).
 */
open class BinaerDatenelement(data: String?, existenzstatus: Existenzstatus, val maxLength: Int? = null)
    : TextDatenelement(data, existenzstatus) {

    @OptIn(ExperimentalStdlibApi::class)
    constructor(data: ByteArray, existenzstatus: Existenzstatus, maxLength: Int? = null) :
            this(data.decodeToString(), existenzstatus, maxLength) // TODO: is this correct?


    /**
     * Für binäre Daten gilt eine besondere Syntaxregelung: Das Auftreten dieser Daten wird eingeleitet mit dem
     * Binärdatenkennzeichen (@). Anschließend folgt die Längenangabe zu den binären Daten und der binäre Wert selbst,
     * der ebenfalls mit dem Binärdatenkennzeichen eingeleitet wird. Die Länge wird angegeben in Byte (nicht die Länge
     * der darstellbaren Zeichen). Hierzu muss sichergestellt sein, dass der binäre Datenstrom in vollen Byte
     * dargestellt werden kann (binäre Daten, die nicht im Byteformat vorliegen, können nicht über FinTS transportiert
     * werden). Syntaxzeichen, die in binären Daten auftreten, dürfen nicht als solche interpretiert werden.
     *
     * Bei Elementen, die entsprechende Zeichen enthalten können (z. B. DE „SEPAName) ist eine base64-Kodierung in der
     * Spezifikation vorzusehen.
     */
    override fun formatValue(value: String): String {
        return "@${value.length}@" + value
    }

    override fun validate() {
        // binary data aren't checked, so they are always valid

        if (writeToOutput) {
            checkIfMandatoryValueIsSet()

            value?.let { value -> // if value is null and value has to be written to output then validation already fails above
                maxLength?.let {
                    if (value.length > maxLength) {
                        throwValidationException("Binäre Daten dürfen nur eine maximale Größe von $maxLength Bytes " +
                                "haben, haben aber ${value.length} Bytes.")
                    }
                }
            }
        }
    }

}