package net.dankito.fints.messages.datenelemente.implementierte.tan

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.fints.messages.datenelemente.implementierte.allCodes


/**
 * Beim Zwei-Schritt-Verfahren werden die notwendigen Prozess-Schritte mittels des Geschäftsvorfalls
 * HKTAN durchgeführt. Dieser unterstützt flexibel vier unterschiedliche Ausprägungen für die beiden
 * Prozessvarianten für Zwei-Schritt-Verfahren, wobei die TAN-Prozesse 3 und 4 nicht isoliert und nur
 * in Verbindung mit TAN-Prozess=2 auftreten können. Der TAN-Prozess wird wie folgt kodiert:
 *
 * Codierung:
 *
 * Prozessvariante 1:
 *
 * TAN-Prozess=1:
 *      Im ersten Schritt wird der Auftrags-Hashwert über den Geschäftsvorfall HKTAN mitgeteilt, im
 *      zweiten Schritt erfolgt nach Ermittlung der TAN aus der zurückgemeldeten Challenge die
 *      Einreichung des eigentlichen Auftrags inklusive der TAN über das normale Auftragssegment.
 *      Abfolge der Segmente am Beispiel HKCCS:
 *      1. Schritt: HKTAN <-> HITAN
 *      2. Schritt: HKCCS <-> HIRMS zu HKCCS
 *
 * Prozessvariante 2:
 *      Im ersten Schritt wird der Auftrag komplett über das normale Auftragssegment eingereicht,
 *      jedoch ohne Übermittlung der TAN. Im zweiten Schritt erfolgt nach Ermittlung der TAN aus
 *      der zurückgemeldeten Challenge die Einreichung der TAN über den Geschäftsvorfall HKTAN.
 *      Abfolge der Segmente am Beispiel HKCCS:
 *      Schritt 1: HKCCS und HKTAN  HITAN
 *      Schritt 2: HKTAN  HITAN und HIRMS zu HICCS
 *
 * TAN-Prozess=2:
 *      kann nur im zweiten Schritt auftreten. Er dient zur Übermittlung der TAN mittels HKTAN,
 *      nachdem der Auftrag selbst zuvor bereits mit TAN-Prozess=3 oder 4 eingereicht wurde.
 *      Dieser Geschäftsvorfall wird mit HITAN, TAN-Prozess=2 beantwortet.
 *
 * TAN-Prozess=3:
 *      kann nur im ersten Schritt bei Mehrfach-TANs für die zweite und ggf. dritte TAN auftreten.
 *      Hierdurch wird die Einreichung eingeleitet, wenn zeitversetzte Einreichung von
 *      Mehrfach-TANs erlaubt ist.
 *
 * TAN-Prozess=4:
 *      kann nur im ersten Schritt auftreten. Hiermit wird das Zwei-Schritt-Verfahren nach
 *      Prozessvariante 2 für die erste TAN eingeleitet. HKTAN wird zusammen mit dem Auftragssegment
 *      übertragen und durch HITAN mit TAN-Prozess=4 beantwortet. TAN-Prozess=4 wird auch beim
 *      Geschäftsvorfall „Prüfen / Verbrennen von TANs“ eingesetzt.
 */
open class TANProzessDatenelement(process: TanProcess) : Code(process.code,
    AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<TanProcess>()
    }

}