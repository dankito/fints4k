package net.dankito.banking.fints.messages.datenelementgruppen.implementierte.encryption

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.implementierte.encryption.*
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Operationsmodus
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.OperationsmodusKodiert
import net.dankito.banking.fints.messages.datenelementgruppen.Datenelementgruppe


/**
 * Angaben zum kryptographischen Algorithmus, zu seinem Operationsmodus, so wie zu
 * dessen Einsatz, in diesem Fall für die Nachrichtenverschlüsselung.
 *
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.9 DEG „Verschlüsselungsalgorithmus“, S. 59):
 *
 * Wert des Algorithmusparameters, Schlüssel
 *      FinTS-Füllwert, z.B. X’00 00 00 00 00 00 00 00’
 *
 * Bezeichner für Algorithmusparameter, Schlüssel
 *      FinTS-Füllwert, z.B. „5“
 *
 * Wert des Algorithmusparameters, IV
 *      Belegung nicht zulässig.
 */
open class VerschluesselungsalgorithmusDatenelementgruppe(
    mode: Operationsmodus,
    encryptionAlgorithm: Verschluesselungsalgorithmus

) : Datenelementgruppe(listOf(
        VerwendungDesVerschluesselungsalgorithmusKodiert(), // allowed: 2
        OperationsmodusKodiert(mode), // allowed: 2, 18, 19
        VerschluesselungsalgorithmusKodiert(encryptionAlgorithm), // allowed: 13, 14
        WertDesAlgorithmusparametersSchluessel(WertDesAlgorithmusparametersSchluessel.FinTsMock),
        BezeichnerFuerAlgorithmusparameterSchluesselDatenelement(BezeichnerFuerAlgorithmusparameterSchluesselDatenelement.FinTsMock), // allowed: 6
        // even though spec says for PIN/TAN no value may be set here ("Belegung nicht zulässig"), this value has to be set:
        BezeichnerFuerAlgorithmusparameterIVDatenelement(BezeichnerFuerAlgorithmusparameterIV.InitializationValue_ClearText, Existenzstatus.Mandatory)
), Existenzstatus.Mandatory)