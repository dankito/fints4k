package net.dankito.fints.messages.datenelementgruppen.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.NotAllowedDatenelement
import net.dankito.fints.messages.datenelemente.implementierte.signatur.BezeichnerFuerSicherheitspartei
import net.dankito.fints.messages.datenelemente.implementierte.signatur.IdentifizierungDerPartei
import net.dankito.fints.messages.datenelementgruppen.Datenelementgruppe


/**
 * Identifikation der im Sicherheitsprozess involvierten Parteien. Dient zur Übermittlung
 * der CID bei kartenbasierten Sicherheitsverfahren bzw. der Kundensystem-ID bei
 * softwarebasierten Verfahren (z. B. Speicherung der Schlüssel in einer Schlüsseldatei).
 *
 * Wenn eine Synchronisierung der Kundensystem-ID durchgeführt wird, ist als Identifizierung der Partei ‚0’ einzustellen.
 *
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.3 DEG „Sicherheitsidentifikation, Details“, S. 58):
 *
 * CID
 *      Dieses Feld darf nicht belegt werden.
 *
 * Identifizierung der Partei
 *      Dieses Feld muss eine gültige, zuvor vom Banksystem angeforderte Kundensystem-ID enthalten
 *      (analog zu RAH-/RDH-Verfahren). Dies gilt auch für Zweit- und Drittsignaturen.
 */
open class SicherheitsidentifikationDetails(partyIdentification: String)
    : Datenelementgruppe(listOf(
        BezeichnerFuerSicherheitspartei(),
        NotAllowedDatenelement(),
        IdentifizierungDerPartei(partyIdentification)
), Existenzstatus.Mandatory)