package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Bezeichner für den Hashalgorithmusparameter.
 *
 * Codierung:
 * 1: IVC (Initialization value, clear text)
 */
open class BezeichnerFuerHashalgorithmusparameter : AlphanumerischesDatenelement("1", Existenzstatus.Mandatory, 3)