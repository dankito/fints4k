package net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.ZiffernDatenelement


/**
 * Kennzeichen gemäß ISO 3166-1 (numerischer Code).
 *
 * (Siehe z.B. PDF Messages_Geschaeftsvorfaelle Kapitel E.4 (S. 813) oder https://en.wikipedia.org/wiki/ISO_3166-1#Current_codes .)
 */
open class Laenderkennzeichen(countryCode: Int?, existenzstatus: Existenzstatus)
    : ZiffernDatenelement(countryCode, 3, existenzstatus) {

    companion object {
        /**
         * Für Deutschland wird der Code 280 verwendet da dieser im Kreditgewerbe gebräuchlicher als der neue Code 276 ist.
         */
        const val Germany = 280
    }

}