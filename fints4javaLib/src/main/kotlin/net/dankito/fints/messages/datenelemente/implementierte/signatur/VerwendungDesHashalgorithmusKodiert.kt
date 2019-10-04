package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Code


/**
 * Kodierte Information über die Verwendung des Hashalgorithmus.
 *
 * Im Zusammenhang mit Hash-Funktionen ist derzeit nur folgender Wert möglich:
 *
 * Codierung:
 * 1: Owner Hashing (OHA)
 */
open class VerwendungDesHashalgorithmusKodiert: Code("1", listOf("1"), Existenzstatus.Mandatory)