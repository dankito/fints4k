package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.banking.fints.messages.datenelemente.implementierte.allCodes


/**
 * Kodierte Information über den Signaturalgorithmus.
 *
 * Codierung:
 * 1: nicht zugelassen
 * 10: RSA-Algorithmus (bei RAH)
 *
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.6 DEG „Signaturalgorithmus“, S. 58):
 *
 * Signaturalgorithmus, kodiert
 *      FinTS-Füllwert, z. B. „10“
 */
open class SignaturalgorithmusKodiert(algorithm: Signaturalgorithmus)
    : Code(algorithm.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<Signaturalgorithmus>()

        val FinTsMockValue = Signaturalgorithmus.RSA_Algorithmus
    }

}