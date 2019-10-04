package net.dankito.fints.messages.datenelemente.implementierte.encryption

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.fints.messages.datenelemente.implementierte.allCodes


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

        val FinTsMock = Verschluesselungsalgorithmus.AES_256
    }

}