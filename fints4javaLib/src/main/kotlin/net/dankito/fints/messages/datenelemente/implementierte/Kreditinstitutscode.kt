package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Landesspezifische Kennung, die das Kreditinstitut eindeutig identifiziert. In Deutschland
 * wird die Bankleitzahl eingestellt. Bei Kreditinstituten, die in Ländern ohne
 * Institutskennungssystem beheimatet sind, kann die Belegung entfallen.
 * Zu weiteren Informationen siehe Kap. E.5.
 */
open class Kreditinstitutscode(bankCode: String, existenzstatus: Existenzstatus)
    : AlphanumerischesDatenelement(bankCode, existenzstatus, 30)