package net.dankito.fints.messages.segmente.accountstatement

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Code
import net.dankito.fints.messages.datenelemente.basisformate.NumerischesDatenelement
import net.dankito.fints.messages.datenelemente.implementierte.Aufsetzpunkt
import net.dankito.fints.messages.datenelemente.implementierte.Kontoauszugsformat
import net.dankito.fints.messages.datenelemente.implementierte.account.MaximaleAnzahlEintraege
import net.dankito.fints.messages.datenelemente.implementierte.allCodes
import net.dankito.fints.messages.datenelementgruppen.Datenelementgruppe
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.CustomerSegmentId


open class KontoauszugAnfordernBase(
    segmentVersion: Int,
    segmentNumber: Int,
    account: Datenelementgruppe,
    format: Kontoauszugsformat? = null,
    accountStatementNumber: Int? = null,
    accountStatementYear: Int? = null,
    maxCountEntries: Int? = null,
    continuationId: String? = null
)
    : Segment(listOf(
    Segmentkopf(CustomerSegmentId.AccountStatementPdf, segmentVersion, segmentNumber),
    account,
    Code(format, allCodes<Kontoauszugsformat>(), Existenzstatus.Mandatory),
    NumerischesDatenelement(accountStatementNumber, 5, Existenzstatus.Optional),
    NumerischesDatenelement(accountStatementYear, 4, Existenzstatus.Optional),
    MaximaleAnzahlEintraege(maxCountEntries, Existenzstatus.Optional), // > 0. O: „Eingabe Anzahl Einträge erlaubt“ (BPD) = „J“. N: sonst
    Aufsetzpunkt(continuationId, Existenzstatus.Optional) // M: vom Institut wurde ein Aufsetzpunkt rückgemeldet. N: sonst
))