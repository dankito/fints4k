package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.NotAllowedDatenelement
import net.dankito.fints.messages.datenelemente.implementierte.encryption.Komprimierungsfunktion
import net.dankito.fints.messages.datenelemente.implementierte.encryption.KomprimierungsfunktionDatenelement
import net.dankito.fints.messages.datenelemente.implementierte.signatur.*
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.datenelementgruppen.implementierte.encryption.VerschluesselungsalgorithmusDatenelementgruppe
import net.dankito.fints.messages.datenelementgruppen.implementierte.signatur.Schluesselname
import net.dankito.fints.messages.datenelementgruppen.implementierte.signatur.SicherheitsdatumUndUhrzeit
import net.dankito.fints.messages.datenelementgruppen.implementierte.signatur.SicherheitsidentifikationDetails
import net.dankito.fints.messages.datenelementgruppen.implementierte.signatur.Sicherheitsprofil
import net.dankito.fints.messages.segmente.Segment


/**
 * Der Verschlüsselungskopf enthält Informationen über die Art des Sicherheitsservice, die
 * Verschlüsselungsfunktion und die zu verwendenden Chiffrierschlüssel.
 *
 * Zum Abgleich mit dem in den BPD definierten RAH-Verschlüsselungsverfahren wird das Feld
 * „Bezeichner für Algorithmusparameter, Schlüssel“ in der DEG „Verschlüsselungsalgorithmus“ herangezogen.
 *
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.8 Segment „Verschlüsselungskopf“, S. 59)
 *
 * Sicherheitsfunktion, kodiert
 *      Es wird der Wert „998“ (Klartext) verwendet.
 *
 * Zertifikat
 *      Dieses Feld darf nicht belegt werden.
 */
open class Verschluesselungskopf(
    method: Sicherheitsverfahren,
    version: VersionDesSicherheitsverfahrens,
    partyIdentification: String,
    date: Int,
    time: Int,
    mode: Operationsmodus,
    bankCountryCode: Int,
    bankCode: String,
    userIdentification: String,
    key: Schluesselart,
    keyNumber: Int,
    keyVersion: Int,
    algorithm: Komprimierungsfunktion

) : Segment(listOf(
    Segmentkopf("HNVSK", 3, 998),
    Sicherheitsprofil(method, version),
    SicherheitsfunktionKodiert(Sicherheitsfunktion.Klartext), // allowed: 4
    RolleDesSicherheitslieferantenKodiert(), // allowed: 1, 4
    SicherheitsidentifikationDetails(partyIdentification),
    SicherheitsdatumUndUhrzeit(date, time),
    VerschluesselungsalgorithmusDatenelementgruppe(mode),
    Schluesselname(bankCountryCode, bankCode, userIdentification, key, keyNumber, keyVersion),
    KomprimierungsfunktionDatenelement(algorithm),
    // Certificate not applicapable for PIN/TAN; it should be also fine to write nothing at all and therefore leave NotAllowedDatenelement away
    NotAllowedDatenelement() // Zertifikat is actually a Datenelementgruppe, not a Datenelement
), Existenzstatus.Mandatory)