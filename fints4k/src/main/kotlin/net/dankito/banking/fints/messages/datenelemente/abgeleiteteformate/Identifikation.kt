package net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Dient der eindeutigen Kennzeichnung von Objekten (z. B. Benutzerkennung, Kontonummer).
 *
 * Maximal 30 Zeichen
 */
abstract class Identifikation(identification: String?, existenzstatus: Existenzstatus)
    : AlphanumerischesDatenelement(identification, existenzstatus, 30)