package net.dankito.fints.messages.segmente.implementierte.umsaetze

import net.dankito.fints.messages.datenelementgruppen.implementierte.account.KontoverbindungInternational
import net.dankito.fints.model.AccountData
import net.dankito.fints.model.BankData


open class SaldenabfrageVersion7(
    segmentNumber: Int,
    account: AccountData,
    bank: BankData,
    allAccounts: Boolean = false,
    maxAmountEntries: Int? = null,
    continuationId: String? = null
)
    : SaldenabfrageBase(segmentNumber, 7, KontoverbindungInternational(account, bank), allAccounts, maxAmountEntries, continuationId)