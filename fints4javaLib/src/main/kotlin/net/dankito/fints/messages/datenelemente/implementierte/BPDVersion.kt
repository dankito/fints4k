package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.basisformate.NumerischesDatenelement


/**
 * Es handelt sich um eine kreditinstitutsseitig vergebene Versionsnummer der
 * Bankparameterdaten (BPD), die den jeweiligen Stand der instituts-spezifischen
 * Unterstützung des Systems kennzeichnet (bei jeder für das Kundensystem relevanten
 * Änderung des Kreditinstitutssystems werden neue BPD mit einer neuen BPD-Versionsnummer
 * kreditinstitutsseitig bereitgestellt).
 *
 * Diese BPD-Versionsnummer ist unabhängig von der Version des BPD-Nachrichtenformats,
 * die im Nachrichtenkopf eingestellt ist und lediglich das syntaktische Format der
 * Nachricht, nicht jedoch deren Inhalt kennzeichnet.
 */
class BPDVersion(version: Int, existenzstatus: Existenzstatus) : NumerischesDatenelement(version, 3, existenzstatus) {

    companion object {
        const val VersionNotReceivedYet = 0
    }

}