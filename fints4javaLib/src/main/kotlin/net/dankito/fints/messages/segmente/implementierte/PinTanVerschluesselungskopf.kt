package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.encryption.Komprimierungsfunktion
import net.dankito.fints.messages.datenelemente.implementierte.signatur.*


open class PinTanVerschluesselungskopf(
    partyIdentification: String,
    date: Int,
    time: Int,
    bankCountryCode: Int,
    bankCode: String,
    userIdentification: String

) : Verschluesselungskopf(
    Sicherheitsverfahren.PIN_TAN_Verfahren,
    VersionDesSicherheitsverfahrens.PIN_Zwei_Schritt,
    partyIdentification,
    date,
    time,
    OperationsmodusKodiert.FinTsMockValue,
    bankCountryCode,
    bankCode,
    userIdentification,
    Schluesselart.Chiffrierschluessel,
    Schluesselnummer.FinTsMockValue,
    Schluesselversion.FinTsMockValue,
    Komprimierungsfunktion.Keine_Kompression
)