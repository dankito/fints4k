package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.banking.fints.messages.datenelemente.implementierte.allCodes


/**
 * Information über die Art des Schlüssels.
 *
 * Beim Sicherheitsverfahren RAH steht die Schlüsselart in engem Zusammenhang mit dem
 * Datenelement "Verwendungszweck für öffentlichen Schlüssel". Die Inhalte beider
 * Datenelemente sind konsistent zu halten.
 *
 * Codierung:
 * D: Schlüssel zur Erzeugung digitaler Signaturen (DS-Schlüssel)
 * S: Signierschlüssel
 * V: Chiffrierschlüssel
 *
 * Der DS-Schlüssel steht nur im Zusammenhang mit einer Bankensignaturkarte zur Verfügung.
 *
 * Im Falle der Bankensignaturkarte ergibt sich folgende Zuordnung zu den Kartenschlüsseln:
 * - DS-Schlüssel: SK.CH.DS
 * - Signierschlüssel: SK.CH.AUT
 * - Chiffrierschlüssel: SK.CH.KE
 */
open class SchluesselartDatenelement(key: Schluesselart) : Code(key.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<Schluesselart>()
    }

}