package net.dankito.banking.fints.messages.datenelemente.implementierte.encryption

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.banking.fints.messages.datenelemente.implementierte.allCodes


/**
 * Kodierte Information über den verwendeten Verschlüsselungsalgorithmus.
 *
 * Codierung:
 * 13: 2-Key-Triple-DES (nicht zugelassen)
 * 14: AES-256 [AES]
 */
open class VerschluesselungsalgorithmusKodiert(algorithm: Verschluesselungsalgorithmus)
    : Code(algorithm.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<Verschluesselungsalgorithmus>()
    }

}