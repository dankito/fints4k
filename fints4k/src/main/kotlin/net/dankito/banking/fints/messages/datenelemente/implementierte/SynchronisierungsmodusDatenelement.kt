package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Code


/**
 * Information über den Synchronisierungsmodus.
 *
 * Codierung:
 * 0: Neue Kundensystem-ID zurückmelden
 * 1: Letzte verarbeitete Nachrichtennummer zurückmelden
 * 2: Signatur-ID zurückmelden
 */
class SynchronisierungsmodusDatenelement(mode: Synchronisierungsmodus)
    : Code(mode.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<Synchronisierungsmodus>()
    }

}