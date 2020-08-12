package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.banking.fints.messages.datenelemente.implementierte.allCodes


/**
 * Information über den Operationsmodus für den jeweils verwendeten Kryptoalgorithmus
 * (zur Signaturbildung oder zur Verschlüsselung).
 *
 * Codierung:
 * Siehe S. 102 oder [Operationsmodus]
 *
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.6 DEG „Signaturalgorithmus“, S. 58):
 *
 * Operationsmodus, kodiert
 *      FinTS-Füllwert, z. B. „16“
 */
open class OperationsmodusKodiert(mode: Operationsmodus) : Code(mode.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<Operationsmodus>()

        val PinTanDefaultValue = Operationsmodus.ISO_9796_1
    }

}