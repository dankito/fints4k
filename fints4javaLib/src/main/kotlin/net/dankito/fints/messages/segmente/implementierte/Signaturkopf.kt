package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.signatur.*
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.datenelementgruppen.implementierte.signatur.*
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.MessageSegmentId
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData


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
    customer: CustomerData,
    securityControlReference: String,
    date: Int,
    time: Int,
    algorithm: Signaturalgorithmus,
    mode: Operationsmodus,
    keyNumber: Int,
    keyVersion: Int

) : Segment(listOf(
    Segmentkopf(MessageSegmentId.SignatureHeader, 4, segmentNumber), // allowed
    Sicherheitsprofil(Sicherheitsverfahren.PIN_TAN_Verfahren, VersionDesSicherheitsverfahrens.Version_2), // fints4java only supports Pin/Tan and PSD2 requires two step tan procedure
    SicherheitsfunktionKodiert(customer.selectedTanProcedure.securityFunction),
    Sicherheitskontrollreferenz(securityControlReference), // allowed: <>0
    BereichDerSicherheitsapplikationKodiert(BereichDerSicherheitsapplikation.SignaturkopfUndHBCINutzdaten), // allowed: 1 ?
    RolleDesSicherheitslieferantenKodiert(), // allowed: 1
    SicherheitsidentifikationDetails(customer.customerSystemId),
    // "Bei softwarebasierten Verfahren wird die Sicherheitsreferenznummer auf Basis des DE Kundensystem-ID und des DE Benutzerkennung der DEG Schlüsselnamen verwaltet.
    Sicherheitsreferenznummer(1), // TODO: is this always 1?
    SicherheitsdatumUndUhrzeit(date, time),
    HashalgorithmusDatenelementgruppe(),
    SignaturalgorithmusDatenelementgruppe(algorithm, mode),
    Schluesselname(bank.countryCode, bank.bankCode, customer.customerId, Schluesselart.Signierschluessel, keyNumber, keyVersion)
))