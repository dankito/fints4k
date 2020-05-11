package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.fints.messages.datenelemente.implementierte.allCodes


/**
 * Abhängig von Sicherheitsprofil und Schlüsseltyp und HBCI-Version ist folgender Wert einzustellen:
 *
 * |    Sicherheitsprofil   |   Schlüsseltyp    |    ab FinTS V3.0  |
 * |        RAH-7           |       S           |           2       |
 * |        RAH-7           |       D           |           1       |
 * |        RAH-9           |       S           |           2       |
 * |        RAH-10          |       S           |           2       |
 *
 *
 * Abweichende Belegung für PIN/TAN Verfahren (Dokument Sicherheitsverfahren PIN/TAN, B.9.4 Segment „Signaturkopf“, S. 58):
 *
 * Sicherheitsfunktion, kodiert
 *      Beim Ein-Schritt-Verfahren ist der Wert „999“ einzustellen, beim Zwei-Schritt-Verfahren der entsprechende
 *      in der BPD mitgeteilte Wert für das konkrete Verfahren „900“ bis „997“ (vgl. Kapitel B.8.2).
 */
open class SicherheitsfunktionKodiert(securityFunction: Sicherheitsfunktion)
    : Code(securityFunction.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<Sicherheitsfunktion>()
    }

}