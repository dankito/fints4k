package net.dankito.fints.messages.datenelemente.basisformate

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.Datenelement


/**
 * Binäre Daten werden unverändert in den FinTS-Datensatz eingestellt. Eine Umwandlung in
 * eine Zeichendarstellung erfolgt nicht. Es ist zu beachten, dass der FinTS-Basiszeichensatz
 * für binäre Daten keine Gültigkeit besitzt. Ferner gelten die speziellen Syntaxregeln für
 * binäre Daten (s. Kap. H.1.3).
 */
open class BinaerDatenelement @JvmOverloads constructor(val data: ByteArray, existenzstatus: Existenzstatus, val maxLength: Int? = null)
    : Datenelement(existenzstatus) {

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
    override fun format(): String {
        return "@${data.size}@" + String(data)
    }

    override fun validate() {
        // binary data aren't checked, so they are always valid

        maxLength?.let {
            if (data.size > maxLength) {
                throwValidationException("Binäre Daten dürfen nur eine maximale Größe von $maxLength Bytes haben, " +
                        "haben aber ${data.size} Bytes.")
            }
        }
    }

}