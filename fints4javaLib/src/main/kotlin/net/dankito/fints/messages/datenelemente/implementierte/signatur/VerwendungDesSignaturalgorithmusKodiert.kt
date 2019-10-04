package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Kodierte Information über die Verwendung des Signaturalgorithmus.
 *
 * Im Zusammenhang mit Signaturbildung ist derzeit nur folgender Wert möglich:
 *
 * Codierung:
 * 6: Owner Signing (OSG)
 */
open class VerwendungDesSignaturalgorithmusKodiert : AlphanumerischesDatenelement("6", Existenzstatus.Mandatory, 3)