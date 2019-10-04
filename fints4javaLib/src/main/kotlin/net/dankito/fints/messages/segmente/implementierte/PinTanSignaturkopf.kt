package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.signatur.*


class PinTanSignaturkopf(
    segmentNumber: Int,
    securityFunction: Sicherheitsfunktion,
    securityControlReference: String,
    /**
     *  Wenn eine Synchronisierung der Kundensystem-ID durchgeführt wird, ist als Identifizierung der Partei ‚0’ einzustellen.
     */
    partyIdentification: String,
    date: Int,
    time: Int,
    bankCountryCode: Int,
    bankCode: String,
    userIdentification: String

) : Signaturkopf(
    segmentNumber,
    Sicherheitsverfahren.PIN_TAN_Verfahren,
    VersionDesSicherheitsverfahrens.PIN_Zwei_Schritt,
    securityFunction,
    securityControlReference,
    partyIdentification,
    date,
    time,
    SignaturalgorithmusKodiert.FinTsMockValue,
    OperationsmodusKodiert.FinTsMockValue,
    bankCountryCode,
    bankCode,
    userIdentification,
    Schluesselnummer.FinTsMockValue,
    Schluesselversion.FinTsMockValue
)