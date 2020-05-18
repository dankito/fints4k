package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Identifikation


/**
 * Institutsweit eindeutige Identifikation des Kunden. Die Vergabe obliegt dem Kreditinstitut.
 * Die Kunden-ID kann beliebige Informationen enthalten. Es steht dem Kreditinstitut frei, ob
 * es jedem Kunden genau eine Kunden-ID zuordnet oder dem Kunden in Abhängigkeit vom Benutzer
 * jeweils eine unterschiedliche Kunden-ID zuordnet.
 */
open class KundenID(customerId: String) : Identifikation(customerId, Existenzstatus.Mandatory) {

    companion object {
        const val Anonymous = "9999999999"
    }

}