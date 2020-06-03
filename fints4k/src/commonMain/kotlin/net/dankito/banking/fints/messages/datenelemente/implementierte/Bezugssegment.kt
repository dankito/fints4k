package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Sofern sich ein Kreditinstitutssegment auf ein bestimmtes Kundensegment bezieht
 * (z. B. Antwortrückmeldung auf einen Kundenauftrag) hat das Kreditinstitut die
 * Segmentnummer des Segments der Kundennachricht einzustellen, auf das sich das
 * aktuelle Segment bezieht (s. DE „Segmentnummer“). In Zusammenhang mit den Angaben
 * zur Bezugsnachricht aus dem Nachrichtenkopf ist hierdurch eine eindeutige Referenz
 * auf das Segment einer Kundennachricht möglich.
 *
 * Falls die Angabe eines Bezugssegments erforderlich ist, ist dieses bei der
 * Formatbeschreibung eines Kreditinstitutsegments angegeben.
 */
abstract class Bezugssegment(segmentNumber: Int, existenzstatus: Existenzstatus)
    : NumerischesDatenelement(segmentNumber, 3, existenzstatus) {


    override fun validate() {
        super.validate()

        number?.let {  // if number is null and number has to be written to output then validation already fails above
            if (number < 1) {
                throwValidationException("Bezug Segmentnummer '$number' muss größer oder gleich 1 sein.")
            }
        }
    }

}