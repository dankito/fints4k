package net.dankito.fints.messages.datenelemente.implementierte.encryption

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.Datenelement


/**
 * For PIN/TAN data doesn't get encrypted at all.
 *
 * It simply gets, prefixed by '@<payload_length>@', appended to VerschluesselteDaten segment header
 */
open class PinTanVerschluesselteDatenDatenelement(val payload: String) : Datenelement(Existenzstatus.Mandatory) {

    override fun format(): String {
        return "@${payload.length}@" + payload
    }

    override fun validate() {
        // payload has already been validated, nothing to do
    }

}