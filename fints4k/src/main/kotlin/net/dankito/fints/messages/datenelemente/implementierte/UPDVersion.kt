package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Versionsnummer der Userparameterdaten (UPD). Bei jeder kreditinstitutsseitigen Änderung
 * wird die Version inkrementiert. (S. auch DE BPD-Version).
 */
open class UPDVersion(version: Int, existenzstatus: Existenzstatus) : NumerischesDatenelement(version, 3, existenzstatus) {

    companion object {
        const val VersionNotReceivedYet = 0
    }

}