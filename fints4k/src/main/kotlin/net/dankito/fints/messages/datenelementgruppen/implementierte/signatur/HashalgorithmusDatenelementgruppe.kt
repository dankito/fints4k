package net.dankito.fints.messages.datenelementgruppen.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.signatur.BezeichnerFuerHashalgorithmusparameter
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Hashalgorithmus
import net.dankito.fints.messages.datenelemente.implementierte.signatur.HashalgorithmusKodiert
import net.dankito.fints.messages.datenelemente.implementierte.signatur.VerwendungDesHashalgorithmusKodiert
import net.dankito.fints.messages.datenelementgruppen.Datenelementgruppe


/**
 * Angaben zu einem kryptographischen Algorithmus, seinen Operationsmodus, sowie dessen Einsatz.
 *
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.5 DEG „Hashalgorithmus“, S. 58):
 *
 * Wert des Hashalgorithmusparameters
 *      Dieses Feld darf nicht belegt werden.
 */
open class HashalgorithmusDatenelementgruppe
    : Datenelementgruppe(listOf(
        VerwendungDesHashalgorithmusKodiert(),
        HashalgorithmusKodiert(Hashalgorithmus.Gegenseitig_vereinbart), // allowed: 3, 4, 5, 6
        BezeichnerFuerHashalgorithmusparameter()
), Existenzstatus.Mandatory)