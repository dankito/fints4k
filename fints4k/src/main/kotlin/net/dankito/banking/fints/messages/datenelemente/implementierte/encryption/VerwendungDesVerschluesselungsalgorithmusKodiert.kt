package net.dankito.banking.fints.messages.datenelemente.implementierte.encryption

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.banking.fints.messages.datenelemente.implementierte.allCodes


/**
 * Kodierte Information über die Verwendung des Verschlüsselungsalgorithmus.
 *
 * Im Zusammenhang mit der Verschlüsselung sind derzeit folgende Werte möglich:
 *
 * Codierung:
 * 2: Owner Symmetric (OSY)
 */
open class VerwendungDesVerschluesselungsalgorithmusKodiert
    : Code(VerwendungDesVerschluesselungsalgorithmus.OwnerSymmetric.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<VerwendungDesVerschluesselungsalgorithmus>()
    }

}