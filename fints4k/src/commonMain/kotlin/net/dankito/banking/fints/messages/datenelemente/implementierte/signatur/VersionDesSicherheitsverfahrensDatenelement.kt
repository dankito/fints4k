package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Version des unterstützten Sicherheitsverfahrens (s. „Sicherheitsverfahren, Code“).
 *
 * In Kombination mit dem Sicherheitsverfahren RAH sind die folgenden Versionen gültig:
 *
 * | Version  |   Signaturverfahren   | Schlüssellänge (bit)    |   Hashverfahren   |   Schlüsselart*   |
 * |    7     |      PKCS#1 PSS       |       ..2048            |      SHA-256      |       D, S, V     |
 * |    9     |      PKCS#1 PSS       |       ..2048            |      SHA-256      |       S, V        |
 * |   10     |      PKCS#1 PSS       |       ..2048            |      SHA-256      |       S, V        |
 *
 * *s. Element „Schlüsselart“
 *
 * Andere als die genannten Profile sind nicht zulässig.
 *
 * Um Multibankfähigkeit zu gewährleisten, ist die Unterstützung des Verfahrens RAH-9 kundenund
 * kreditisinstitutsseitig verpflichtend.
 *
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.1 DEG „Sicherheitsprofil“, S. 58):
 *
 * Version des Sicherheitsverfahrens
 * - „1“ : bei allen Nachrichten, wenn Dialog im Einschritt-Verfahren
 * - „2“ : bei allen Nachrichten, wenn Dialog im Zwei-Schritt-Verfahren

 */
open class VersionDesSicherheitsverfahrensDatenelement(version: VersionDesSicherheitsverfahrens)
    : NumerischesDatenelement(version.methodNumber, 3, Existenzstatus.Mandatory)