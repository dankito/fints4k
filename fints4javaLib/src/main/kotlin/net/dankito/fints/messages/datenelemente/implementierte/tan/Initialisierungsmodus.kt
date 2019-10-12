package net.dankito.fints.messages.datenelemente.implementierte.tan

import net.dankito.fints.messages.datenelemente.implementierte.ICodeEnum


/**
 * Bezeichnet das Verfahren, welches bei Verwendung von PIN/TAN während der Dialoginitialisierung verwendet wird und
 * bezieht sich dabei auf die in der Spezifikation des HandHeldDevice [HHD] bzw. den Belegungsrichtlinien [HHD-Belegung]
 * definierten Schablonen 01 und 02.
 *
 * Die Schablonen werden in [HHD] zwar begrifflich auch als „Challengeklassen“ bezeichnet, sind jedoch Bestandteil des
 * dort definierten „Start-Code“, der in Ausgaberichtung im FinTS Datenelement „Challenge“ übertragen wird und daher nicht
 * zu verwechseln mit der „Challengeklasse“ im Sinne einer Geschäftsvorfallsklasse bei HKTAN in der Prozessvariante 1.
 */
enum class Initialisierungsmodus(override val code: String) : ICodeEnum {

    InitialisierungsverfahrenMitKlartextPinOhneTan("00"),

    VerwendungAnalogDerInHhdBeschriebenenSchablone01_VerschluesseltePinOhneTan("01"),

    VerwendungAnalogDerInHhdBeschriebenenSchablone02_ReserviertBeiFinTsDerzeitNichtVerwendet("02")

}