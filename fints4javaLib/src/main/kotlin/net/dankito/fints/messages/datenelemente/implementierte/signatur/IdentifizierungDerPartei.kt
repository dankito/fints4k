package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Identifikation


/**
 * Code, welcher die (Kommunikations-)Partei identifiziert. Bei Verwendung des
 * RAH-Verfahrens ist die Kundensystem-ID einzustellen.
 */
open class IdentifizierungDerPartei(identification: String) : Identifikation(identification, Existenzstatus.Optional) {

    companion object {
        const val SynchronizingCustomerSystemId = "0"
    }

}