package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Referenzinformation, mit der die Verbindung zwischen Signaturkopf und dazu gehörigem
 * Signaturabschluss hergestellt werden kann. Die Sicherheitskontrollreferenz im
 * Signaturkopf muss mit der entsprechenden Information im Signaturabschluss übereinstimmen.
 */
open class Sicherheitskontrollreferenz(reference: String) : AlphanumerischesDatenelement(reference, Existenzstatus.Mandatory, 14)