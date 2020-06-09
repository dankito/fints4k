package net.dankito.banking.fints.messages.datenelementgruppen.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.PinOrTan
import net.dankito.banking.fints.messages.datenelementgruppen.Datenelementgruppe


/**
 * Bei nicht-schlüsselbasierten Sicherheitsverfahren kann der Benutzer hier Angaben
 * zur Authentisierung machen. Ob das Feld verpflichtend ist, ist vom jeweiligen
 * Sicherheitsverfahren abhängig.
 *
 * Format: s. Spezifikation „Sicherheitsverfahren PIN/TAN“
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.7 Segment „Signaturabschluss“, S. 59)
 *
 * Es ist der Signaturabschluss gemäß [HBCI] ab Segmentversion 2 zu verwenden.
 *
 * Validierungsresultat
 *      Dieses Feld darf nicht belegt werden.
 *
 * Benutzerdefinierte Signatur
 *      Hier werden bei Verwendung des PIN/TAN-Verfahrens PIN und TAN eingestellt. Bei
 *      Verwendung des Zwei-Schritt-Verfahrens mit Prozessvariante 2 darf eine TAN
 *      ausschließlich über den Geschäftsvorfall HKTAN eingereicht werden, wobei pro
 *      HKTAN nur die Verarbeitung einer einzelnen TAN zulässig ist. Ansonsten darf die
 *      DE „TAN“ im Signaturabschluss nicht belegt werden; ihr Inhalt wird in diesem Fall
 *      ignoriert und die TAN vom Institut entwertet. Gleiches gilt bei der nicht
 *      zulässigen Übermittlung von mehreren TANs mit HKTAN. Bei der Verwendung im Rahmen
 *      des Sicherheitsverfahrens HBCI darf die DEG nicht belegt werden. Ihr Inhalt wird
 *      in diesem Fall ignoriert.
 */
open class BenutzerdefinierteSignatur(pin: String, tan: String? = null)
    : Datenelementgruppe(listOf(
        PinOrTan(pin, Existenzstatus.Mandatory),
        PinOrTan(tan, Existenzstatus.Optional)
), Existenzstatus.Mandatory)