package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.signatur.*
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.datenelementgruppen.implementierte.signatur.*
import net.dankito.fints.messages.segmente.Segment


/**
 * Der Signaturkopf enthält Informationen über den damit verbundenen Sicherheitsservice, sowie über den Absender.
 *
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.4 Segment „Signaturkopf“, S. 58):
 *
 * Sicherheitsfunktion, kodiert
 *      Beim Ein-Schritt-Verfahren ist der Wert „999“ einzustellen, beim Zwei-Schritt-Verfahren der entsprechende
 *      in der BPD mitgeteilte Wert für das konkrete Verfahren „900“ bis „997“ (vgl. Kapitel B.8.2).
 *
 * Zertifikat
 *      Dieses Feld darf nicht belegt werden.
 */
open class Signaturkopf(
    segmentNumber: Int,
    method: Sicherheitsverfahren,
    version: VersionDesSicherheitsverfahrens,
    securityFunction: Sicherheitsfunktion,
    securityControlReference: String,
    /**
     *  Wenn eine Synchronisierung der Kundensystem-ID durchgeführt wird, ist als Identifizierung der Partei ‚0’ einzustellen.
     */
    partyIdentification: String,
    date: Int,
    time: Int,
    algorithm: Signaturalgorithmus,
    mode: Operationsmodus,
    bankCountryCode: Int,
    bankCode: String,
    userIdentification: String,
    keyNumber: Int,
    keyVersion: Int

) : Segment(listOf(
    Segmentkopf("HNSHK", 4, segmentNumber), // allowed
    Sicherheitsprofil(method, version), // allowed: method: RAH, PIN;
    SicherheitsfunktionKodiert(securityFunction), // allowed: 1, 2
    Sicherheitskontrollreferenz(securityControlReference), // allowed: <>0
    BereichDerSicherheitsapplikationKodiert(BereichDerSicherheitsapplikation.SignaturkopfUndHBCINutzdaten), // allowed: 1 ?
    RolleDesSicherheitslieferantenKodiert(), // allowed: 1
    SicherheitsidentifikationDetails(partyIdentification),
    // "Bei softwarebasierten Verfahren wird die Sicherheitsreferenznummer auf Basis des DE Kundensystem-ID und des DE Benutzerkennung der DEG Schlüsselnamen verwaltet.
    Sicherheitsreferenznummer(1), // TODO: is this always 1?
    SicherheitsdatumUndUhrzeit(date, time),
    HashalgorithmusDatenelementgruppe(),
    SignaturalgorithmusDatenelementgruppe(algorithm, mode),
    Schluesselname(bankCountryCode, bankCode, userIdentification, Schluesselart.Signierschluessel, keyNumber, keyVersion)
), Existenzstatus.Mandatory)