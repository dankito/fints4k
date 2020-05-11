package net.dankito.fints.messages.datenelemente.implementierte.signatur

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.fints.messages.datenelemente.implementierte.allCodes


/**
 * Code des verwendeten Hash-Algorithmus.
 *
 * Codierung:
 * 1: SHA-1 (nicht zugelassen)
 * 2: belegt
 * 3: SHA-256
 * 4: SHA-384
 * 5: SHA-512
 * 6: SHA-256 / SHA-256
 * 999: Gegenseitig vereinbart (ZZZ); (nicht zugelassen)
 */
open class HashalgorithmusKodiert(algorithm: Hashalgorithmus) : Code(algorithm.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<Hashalgorithmus>()
    }

}