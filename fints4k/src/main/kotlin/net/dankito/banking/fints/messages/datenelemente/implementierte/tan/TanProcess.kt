package net.dankito.banking.fints.messages.datenelemente.implementierte.tan

import net.dankito.banking.fints.messages.datenelemente.implementierte.ICodeEnum


/**
 * Beim Zwei-Schritt-Verfahren werden die notwendigen Prozess-Schritte mittels des Geschäftsvorfalls HKTAN durchgeführt.
 * Dieser unterstützt flexibel vier unterschiedliche Ausprägungen für die beiden Prozessvarianten für
 * Zwei-Schritt-Verfahren, wobei die TAN-Prozesse 3 und 4 nicht isoliert und nur in Verbindung mit TAN-Prozess=2 auftreten können.
 */
enum class TanProcess(override val code: String) : ICodeEnum {

    /**
     * Im ersten Schritt wird der Auftrags-Hashwert über den Geschäftsvorfall HKTAN mitgeteilt, im zweiten Schritt
     * erfolgt nach Ermittlung der TAN aus der zurückgemeldeten Challenge die Einreichung des eigentlichen Auftrags
     * inklusive der TAN über das normale Auftragssegment. Abfolge der Segmente am Beispiel HKCCS:
     * 1. Schritt: HKTAN <-> HITAN
     * 2. Schritt: HKCCS <-> HIRMS zu HKCCS
     */
    TanProcess1("1"),

    /**
     * Prozessvariante 2:
     *
     * Im ersten Schritt wird der Auftrag komplett über das normale Auftragssegment eingereicht, jedoch ohne
     * Übermittlung der TAN. Im zweiten Schritt erfolgt nach Ermittlung der TAN aus der zurückgemeldeten Challenge die
     * Einreichung der TAN über den Geschäftsvorfall HKTAN.
     * Abfolge der Segmente am Beispiel HKCCS:
     * Schritt 1: HKCCS und HKTAN <-> HITAN
     * Schritt 2: HKTAN <-> HITAN und HIRMS zu HICCS
     */

    /**
     * kann nur im zweiten Schritt auftreten. Er dient zur Übermittlung der TAN mittels HKTAN, nachdem der Auftrag
     * selbst zuvor bereits mit TAN-Prozess=3 oder 4 eingereicht wurde.
     * Dieser Geschäftsvorfall wird mit HITAN, TAN-Prozess=2 beantwortet.
     */
    TanProcess2("2"),

    /**
     * kann nur im ersten Schritt bei Mehrfach-TANs für die zweite und ggf. dritte TAN auftreten. Hierdurch wird die
     * Einreichung eingeleitet, wenn zeitversetzte Einreichung von Mehrfach-TANs erlaubt ist.
     */
    TanProcess3("3"),

    /**
     * kann nur im ersten Schritt auftreten. Hiermit wird das Zwei-Schritt-Verfahren nach Prozessvariante 2 für die
     * erste TAN eingeleitet. HKTAN wird zusammen mit dem Auftragssegment übertragen und durch HITAN mit TAN-Prozess=4
     * beantwortet. TAN-Prozess=4 wird auch beim Geschäftsvorfall „Prüfen / Verbrennen von TANs“ eingesetzt.
     */
    TanProcess4("4"),

    AppTan("S") // TODO: what is this?

}