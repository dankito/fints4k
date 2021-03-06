package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Segmentspezifische Kennung, die jedem Segment bzw. Auftrag zugeordnet ist (z. B. "HKCCS"
 * für "SEPA Einzelüberweisung"). Die Angabe hat in Großschreibung zu erfolgen.
 */
open class Segmentkennung(identifier: String): AlphanumerischesDatenelement(identifier, Existenzstatus.Mandatory, 6)