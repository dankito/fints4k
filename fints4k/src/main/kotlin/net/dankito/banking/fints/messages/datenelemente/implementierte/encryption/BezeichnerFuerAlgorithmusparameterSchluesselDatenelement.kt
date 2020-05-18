package net.dankito.banking.fints.messages.datenelemente.implementierte.encryption

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.banking.fints.messages.datenelemente.implementierte.allCodes


/**
 * Eigenschaft des Schlüssels für die RAH-Verfahren (Die Steuerung erfolgt in den BPD, vgl. [Formals]).
 *
 * Codierung:
 * 5: Symmetrischer Schlüssel (nicht zugelassen)
 * 6: Symmetrischer Schlüssel, verschlüsselt mit einem öffentlichen Schlüssel bei RAH und RDH (KYP).
 */
open class BezeichnerFuerAlgorithmusparameterSchluesselDatenelement(identifier: BezeichnerFuerAlgorithmusparameterSchluessel)
    : Code(identifier.code, AllowedValues, Existenzstatus.Mandatory) {

    companion object {
        val AllowedValues = allCodes<BezeichnerFuerAlgorithmusparameterSchluessel>()

        val FinTsMock = BezeichnerFuerAlgorithmusparameterSchluessel.SymmetrischerSchluessel
    }

}