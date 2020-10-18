package net.dankito.banking.fints.messages.datenelemente.implementierte.account

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.basisformate.NumerischesDatenelement
import net.dankito.banking.fints.model.GetTransactionsParameter


/**
 * Maximale Anzahl rückzumeldender Einträge bei Abholaufträgen, Kreditinstitutsangeboten
 * oder –informationen (vgl. [Formals], Kap. B.6.3).
 */
open class MaximaleAnzahlEintraege(maxAmount: Int?, existenzstatus: Existenzstatus) : NumerischesDatenelement(maxAmount, 4, existenzstatus) {

    constructor(parameter: GetTransactionsParameter) : this(parameter.maxCountEntriesIfSettingItIsAllowed, if (parameter.isSettingMaxCountEntriesAllowedByBank) Existenzstatus.Optional else Existenzstatus.NotAllowed) // > 0. O: „Eingabe Anzahl Einträge erlaubt“ (BPD) = „J“. N: sonst

}