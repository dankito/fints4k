package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Bezeichner für den Hashalgorithmusparameter.
 *
 * Codierung:
 * 1: IVC (Initialization value, clear text)
 */
open class BezeichnerFuerHashalgorithmusparameter : AlphanumerischesDatenelement("1", Existenzstatus.Mandatory, 3)