package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.banking.fints.messages.datenelemente.implementierte.allCodes


/**
 * Code des unterstützten Signatur- bzw. Verschlüsselungsalgorithmus.
 *
 * Weitere Informationen zu den Verfahren sind Kapitel B.1 zu entnehmen.
 *
 * Codierung:
 * - RAH: RSA-AES-Hybridverfahren
 * - PIN: PIN/TAN-Verfahren
 */
open class SicherheitsverfahrenCode(method: Sicherheitsverfahren) : Code(method.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<Sicherheitsverfahren>()
    }

}