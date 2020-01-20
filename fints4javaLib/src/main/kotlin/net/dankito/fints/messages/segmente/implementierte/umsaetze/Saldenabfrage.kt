package net.dankito.fints.messages.segmente.implementierte.umsaetze

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.Aufsetzpunkt
import net.dankito.fints.messages.datenelemente.implementierte.account.AlleKonten
import net.dankito.fints.messages.datenelemente.implementierte.account.MaximaleAnzahlEintraege
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.datenelementgruppen.implementierte.account.Kontoverbindung
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.fints.model.AccountData


open class Saldenabfrage(
    segmentNumber: Int,
    account: AccountData,
    allAccounts: Boolean = false,
    maxAmountEntries: Int? = null,
    continuationId: String? = null
)
    : Segment(listOf(
        Segmentkopf(CustomerSegmentId.Balance, 5, segmentNumber),
        Kontoverbindung(account),
        AlleKonten(allAccounts, Existenzstatus.Mandatory),
        MaximaleAnzahlEintraege(maxAmountEntries, Existenzstatus.Optional),
        Aufsetzpunkt(continuationId, Existenzstatus.Optional)
))