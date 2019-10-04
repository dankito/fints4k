package net.dankito.fints.messages.datenelemente.abgeleiteteformate

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Dient der eindeutigen Kennzeichnung von Objekten (z. B. Benutzerkennung, Kontonummer).
 *
 * Maximal 30 Zeichen
 */
abstract class Identifikation(identification: String, existenzstatus: Existenzstatus)
    : AlphanumerischesDatenelement(identification, existenzstatus, 30)