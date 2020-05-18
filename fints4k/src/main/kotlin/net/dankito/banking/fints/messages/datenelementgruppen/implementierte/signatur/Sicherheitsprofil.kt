package net.dankito.banking.fints.messages.datenelementgruppen.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.Sicherheitsverfahren
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.SicherheitsverfahrenCode
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrens
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrensDatenelement
import net.dankito.banking.fints.messages.datenelementgruppen.Datenelementgruppe


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
    val method: Sicherheitsverfahren,
    val version: VersionDesSicherheitsverfahrens
) : Datenelementgruppe(listOf(
    SicherheitsverfahrenCode(method),
    VersionDesSicherheitsverfahrensDatenelement(version)
), Existenzstatus.Mandatory) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Sicherheitsprofil) return false

        if (method != other.method) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = method.hashCode()
        result = 31 * result + version.hashCode()
        return result
    }


    override fun toString(): String {
        return "$method ${version.methodNumber}"
    }

}