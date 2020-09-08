package net.dankito.banking.fints.messages.segmente.implementierte

import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.*
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.signatur.*
import net.dankito.banking.fints.messages.segmente.Segment
import net.dankito.banking.fints.messages.segmente.id.MessageSegmentId
import net.dankito.banking.fints.model.BankData


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
    bank: BankData,
    versionOfSecurityProcedure: VersionDesSicherheitsverfahrens,
    securityControlReference: String,
    date: Int,
    time: Int,
    algorithm: Signaturalgorithmus,
    mode: Operationsmodus,
    keyNumber: Int,
    keyVersion: Int

) : Segment(listOf(
    Segmentkopf(MessageSegmentId.SignatureHeader, 4, segmentNumber), // allowed
    Sicherheitsprofil(
        Sicherheitsverfahren.PIN_TAN_Verfahren,
        versionOfSecurityProcedure
    ), // fints4k only supports Pin/Tan and PSD2 requires two step tan procedure; the only exception is the first dialog to get user's TAN procedures which allows to use one step tan procedure (as we don't know TAN procedures yet)
    SicherheitsfunktionKodiert(bank.selectedTanProcedure.securityFunction),
    Sicherheitskontrollreferenz(securityControlReference), // allowed: <>0
    BereichDerSicherheitsapplikationKodiert(BereichDerSicherheitsapplikation.SignaturkopfUndHBCINutzdaten), // allowed: 1 ?
    RolleDesSicherheitslieferantenKodiert(), // allowed: 1
    SicherheitsidentifikationDetails(bank.customerSystemId),
    // "Bei softwarebasierten Verfahren wird die Sicherheitsreferenznummer auf Basis des DE Kundensystem-ID und des DE Benutzerkennung der DEG Schlüsselnamen verwaltet.
    Sicherheitsreferenznummer(1), // TODO: is this always 1?
    SicherheitsdatumUndUhrzeit(date, time),
    HashalgorithmusDatenelementgruppe(),
    SignaturalgorithmusDatenelementgruppe(algorithm, mode),
    Schluesselname(bank, Schluesselart.Signierschluessel, keyNumber, keyVersion)
))