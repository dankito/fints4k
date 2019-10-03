package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Information zur eindeutigen Identifizierung eines Segments innerhalb einer Nachricht.
 * Die Segmente einer Nachricht werden in Einerschritten streng monoton aufsteigend nummeriert.
 * Die Nummerierung beginnt mit 1 im ersten Segment der Nachricht (Nachrichtenkopf).
 */
open class Segmentnummer(number: Int) : NumerischesDatenelement(number, 3, Existenzstatus.Mandatory) {

    override fun validate() {
        super.validate()

        if (value < 1) {
            throwValidationException("Segmentnummer '$value' muss größer oder gleich 1 sein.")
        }
    }

}