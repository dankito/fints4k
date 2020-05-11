package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.fints.messages.datenelemente.implementierte.allCodes


/**
 * Enthält die Bedeutung des Zeitstempels.
 *
 * Codierung:
 * 1: Sicherheitszeitstempel (STS)
 * 6: Certificate Revocation Time (CRT)
 */
open class DatumUndZeitbezeichnerKodiert(identifier: DatumUndZeitbezeichner)
    : Code(identifier.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<DatumUndZeitbezeichner>()
    }

}