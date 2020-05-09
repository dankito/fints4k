package net.dankito.fints.messages.segmente.accountstatement

import net.dankito.fints.messages.datenelemente.implementierte.Kontoauszugsformat
import net.dankito.fints.messages.datenelementgruppen.implementierte.account.KontoverbindungInternational
import net.dankito.fints.model.AccountData
import net.dankito.fints.model.BankData


open class KontoauszugAnfordernVersion4(
    segmentNumber: Int,
    bank: BankData,
    account: AccountData,
    format: Kontoauszugsformat? = null,
    accountStatementNumber: Int? = null,
    accountStatementYear: Int? = null,
    maxCountEntries: Int? = null,
    continuationId: String? = null
)
: KontoauszugAnfordernBase(4, segmentNumber, KontoverbindungInternational(account, bank),
    format, accountStatementNumber, accountStatementYear, maxCountEntries, continuationId)