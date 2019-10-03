package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Identifikation


/**
 * Eindeutige Kennzeichnung des Kundensystems, die in Kombination mit der Signatur-ID die
 * Validität (Eindeutigkeit) der Signatur sichert.
 *
 * Die Kundensystem-ID ist nicht eindeutig für das Endgerät (PC), sondern für die Anwendung
 * auf einem Endgerät, d. h., wenn der Kunde auf einem Endgerät mit mehreren Homebanking-
 * Anwendungen arbeitet, muss für jede Anwendung eine eigene Kundensystem-ID geführt werden.
 *
 * Die Kundensystem-ID ist beim HBCI RAH- / RDH- sowie dem PIN/TAN-Verfahren erforderlich.
 * Bei der Verwendung von RAH-/RDH-Chipkarten ab Sicherheitsprofil-Version 3 wird anstatt
 * der Kundensystem-ID die CID der gesteckten Karte verwendet. Beim HBCI DDV-Verfahren und
 * bei TAN-Verfahren ist dieses DE mit dem Wert 0 zu belegen.
 */
open class KundensystemID(customerSystemId: String) : Identifikation(customerSystemId, Existenzstatus.Mandatory) {

    companion object {
        const val Anonymous = "0"
    }

}