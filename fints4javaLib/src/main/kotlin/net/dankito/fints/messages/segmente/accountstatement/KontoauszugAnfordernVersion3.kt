package net.dankito.fints.messages.segmente.accountstatement

import net.dankito.fints.messages.datenelemente.implementierte.Kontoauszugsformat
import net.dankito.fints.messages.datenelementgruppen.implementierte.account.Kontoverbindung
import net.dankito.fints.model.AccountData


open class KontoauszugAnfordernVersion3(
    segmentNumber: Int,
    account: AccountData,
    format: Kontoauszugsformat? = null,
    accountStatementNumber: Int? = null,
    accountStatementYear: Int? = null,
    maxCountEntries: Int? = null,
    continuationId: String? = null
)
: KontoauszugAnfordernBase(3, segmentNumber, Kontoverbindung(account),
    format, accountStatementNumber, accountStatementYear, maxCountEntries, continuationId)