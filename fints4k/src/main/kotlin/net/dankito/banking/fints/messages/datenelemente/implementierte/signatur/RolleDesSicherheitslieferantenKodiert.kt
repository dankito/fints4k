package net.dankito.banking.fints.messages.datenelemente.implementierte.signatur

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement


/**
 * Der Inhalt dieses Feldes sollte derzeit nicht ausgewertet werden. Optional können aber
 * die nachfolgenden Festlegungen angewendet werden, sofern dies zwischen Kunde und
 * Kreditinstitut zuvor vereinbart wurde:
 *
 * 1. Dialoginitialisierung und -ende:
 * Die Rolle wird durch den Dialogführenden bestimmt. Es ist nur eine Signatur erlaubt.
 * Erlaubt ist nur der Wert ISS/wert12.
 *
 * 2. Auftragsnachricht:
 * Grundsätzlich gilt: Sobald die Rolle „WIT“ verwendet wird, muss dieser Benutzer mit der
 * Benutzerkennung aus der Dialoginitialisierung arbeiten. Auch der Benutzer „WIT“ muss
 * bankseitig entsprechend der Auftragsart am Konto des Benutzers „ISS“ berechtigt sein.
 * Die Reihenfolge der Signaturen ist beliebig.
 *
 * [Tabelle mit Werten]
 *
 * Auch bei Belegung dieses Feldes kann das Kundenprodukt nicht davon ausgehen, dass das
 * Feld kreditinstitutsseitig ausgewertet wird.
 */
open class RolleDesSicherheitslieferantenKodiert : AlphanumerischesDatenelement("1", Existenzstatus.Mandatory, 3)