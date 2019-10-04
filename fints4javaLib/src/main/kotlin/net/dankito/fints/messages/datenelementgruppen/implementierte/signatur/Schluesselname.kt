package net.dankito.fints.messages.datenelementgruppen.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.Benutzerkennung
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Schluesselart
import net.dankito.fints.messages.datenelemente.implementierte.signatur.SchluesselartDatenelement
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Schluesselnummer
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Schluesselversion
import net.dankito.fints.messages.datenelementgruppen.Datenelementgruppe
import net.dankito.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung


/**
 * Verwendeter Schlüsselname in strukturierter Form. Mit dieser Information kann die Referenz
 * auf einen Schlüssel hergestellt werden.
 *
 * Dabei enthält das DE „Benutzerkennung“ bei Schlüsseln des Kunden die Benutzerkennung, mit
 * der der Kunde eindeutig identifiziert wird. Bei Schlüsseln des Kreditinstituts ist dagegen
 * eine beliebige Kennung einzustellen, die dazu dient, den Kreditinstitutsschlüssel eindeutig
 * zu identifizieren. Diese Kennung darf weder einer anderen gültigen Benutzerkennung des
 * Kreditinstituts noch der Benutzerkennung für den anonymen Zugang entsprechen.
 *
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.2 DEG „Schlüsselname“, S. 58):
 *
 * Schlüsselnummer
 *      FinTS-Füllwert, z. B. „0“
 *
 * Schlüsselversion
 *      FinTS-Füllwert, z. B. „0“
 */
open class Schluesselname(
    bankCountryCode: Int,
    bankCode: String,
    userIdentification: String,
    key: Schluesselart,
    keyNumber: Int,
    keyVersion: Int
)
    : Datenelementgruppe(listOf(
    Kreditinstitutskennung(bankCountryCode, bankCode),
        Benutzerkennung(userIdentification),
    SchluesselartDatenelement(key),
    Schluesselnummer(keyNumber),
    Schluesselversion(keyVersion)
), Existenzstatus.Mandatory)