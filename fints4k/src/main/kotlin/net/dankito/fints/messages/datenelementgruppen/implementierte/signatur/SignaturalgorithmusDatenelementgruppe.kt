package net.dankito.fints.messages.datenelementgruppen.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.signatur.*
import net.dankito.fints.messages.datenelementgruppen.Datenelementgruppe


/**
 * Angaben zum kryptographischen Algorithmus, zu seinem Operationsmodus,
 * so wie zu dessen Einsatz, in diesem Fall für die Signaturbildung über RAH.
 *
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.6 DEG „Signaturalgorithmus“, S. 58):
 *
 * Signaturalgorithmus, kodiert
 *      FinTS-Füllwert, z. B. „10“
 *
 * Operationsmodus, kodiert
 *      FinTS-Füllwert, z. B. „16“
 */
open class SignaturalgorithmusDatenelementgruppe (algorithm: Signaturalgorithmus, mode: Operationsmodus)
    : Datenelementgruppe(listOf(
        VerwendungDesSignaturalgorithmusKodiert(),
        SignaturalgorithmusKodiert(algorithm),
        OperationsmodusKodiert(mode)
), Existenzstatus.Mandatory)