package net.dankito.fints.messages.datenelementgruppen.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Sicherheitsverfahren
import net.dankito.fints.messages.datenelemente.implementierte.signatur.SicherheitsverfahrenCode
import net.dankito.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrens
import net.dankito.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrensDatenelement
import net.dankito.fints.messages.datenelementgruppen.Datenelementgruppe


/**
 * Verfahren zur Absicherung der Transaktionen, das zwischen Kunde und Kreditinstitut
 * vereinbar wurde. Das Sicherheitsprofil wird anhand der Kombination der beiden Elemente
 * „Sicherheitsverfahren“ und „Version“ bestimmt (z. B. RDH-9). Für das Sicherheitsverfahren
 * PINTAN ist als Code der Wert PIN und als Version der Wert 1 einzustellen.
 *
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.1 DEG „Sicherheitsprofil“, S. 58):
 *
 * Sicherheitsverfahren, Code
 * - „PIN“ : bei allen Nachrichten
 *
 * Version des Sicherheitsverfahrens
 * - „1“ : bei allen Nachrichten, wenn Dialog im Einschritt-Verfahren
 * - „2“ : bei allen Nachrichten, wenn Dialog im Zwei-Schritt-Verfahren
 */
open class Sicherheitsprofil(
    method: Sicherheitsverfahren,
    version: VersionDesSicherheitsverfahrens
) : Datenelementgruppe(listOf(
    SicherheitsverfahrenCode(method),
    VersionDesSicherheitsverfahrensDatenelement(version)
), Existenzstatus.Mandatory)