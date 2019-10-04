package net.dankito.fints.messages.datenelemente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Code


/**
 * Über dieses DE spezifiziert der Kunde die Sprache, in der er im laufenden Dialog
 * mit dem Kreditinstitut kommunizieren möchte. Rückmeldungen und Kreditinstitutsmeldungen
 * werden (soweit kreditinstitutsseitig unterstützt) in der zuvor spezifizierten Sprache
 * an den Kunden übermittelt. Damit verbunden wird ein zugehöriger FinTS-Basiszeichensatz
 * (s. Kap. B.1), der sich durch einen ISO 8859-Codeset und einen ISO 8859-Subset definiert,
 * ausgewählt. Die Definition des Subsets ist den Anlagen (Kap. I.3) zu entnehmen. Der
 * Codeset soll ermöglichen, zu einem späteren Zeitpunkt evtl. auch nicht-lateinische
 * Zeichensätze zuzulassen.
 *
 * Codierung:
 * - 0: Standard
 * - 1: Deutsch, Code ‚de’ (German), Subset Deutsch, Codeset 1 (Latin 1)
 * - 2: Englisch, Code ‚en’ (English), Subset Englisch, Codeset 1 (Latin 1)
 * - 3: Französisch, Code ‚fr’ (French), Subset Französisch, Codeset 1 (Latin 1)
 */
open class DialogspracheDatenelement(language: Dialogsprache, existenzstatus: Existenzstatus)
    : Code(language.code, AllowedValues, existenzstatus) {

    companion object {
        val AllowedValues = allCodes<Dialogsprache>()
    }

}