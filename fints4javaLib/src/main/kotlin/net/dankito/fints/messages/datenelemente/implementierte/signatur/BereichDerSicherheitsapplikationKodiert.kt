package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.fints.messages.datenelemente.implementierte.allCodes


/**
 * Information darüber, welche Daten vom kryptographischen Prozess verarbeitet werden.
 * Diese Information wird benötigt um z. B. zwischen relevanter und belangloser Reihenfolge
 * von Signaturen zu unterscheiden (vgl. [HBCI], Kapitel VI.4).
 *
 * Wenn SHM gewählt wird, so bedeutet dies, dass nur über den eigenen Signaturkopf sowie die
 * HBCI-Nutzdaten ein Hashwert gebildet wird, der in die Signatur eingeht. Dies entspricht
 * bei Mehrfachsignaturen einer bedeutungslosen Reihenfolge.
 *
 * Wenn SHT gewählt wird, dann werden auch alle schon vorhandenen Signaturköpfe und
 * -abschlüsse mitsigniert. Das heißt, dass die Reihenfolge der Signaturen relevant ist.
 *
 * Codierung:
 * - 1: Signaturkopf und HBCI-Nutzdaten (SHM)
 * - 2: Von Signaturkopf bis Signaturabschluss (SHT)
 */
open class BereichDerSicherheitsapplikationKodiert(domain: BereichDerSicherheitsapplikation)
    : Code(domain.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<BereichDerSicherheitsapplikation>()
    }

}