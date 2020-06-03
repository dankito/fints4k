package net.dankito.banking.fints.messages.datenelemente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Code


/**
 * Information darüber, ob die Kundensystem-ID erforderlich ist:
 *
 * Codierung:
 *
 * 0: Kundensystem-ID wird nicht benötigt (HBCI DDV-Verfahren und chipkartenbasierte Verfahren ab Sicherheitsprofil-Version 3)
 *
 * 1: Kundensystem-ID wird benötigt (sonstige HBCI RAH- / RDH- und PIN/TAN-Verfahren)
 */
open class KundensystemStatus(status: KundensystemStatusWerte, existenzstatus: Existenzstatus)
    : Code(status.code, AllowedValues, existenzstatus) {

    companion object {
        val AllowedValues = allCodes<KundensystemStatusWerte>()

        val SynchronizingCustomerSystemId = KundensystemStatusWerte.NichtBenoetigt
    }

}