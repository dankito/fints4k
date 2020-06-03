package net.dankito.banking.fints.messages.segmente.implementierte.umsaetze

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.implementierte.Aufsetzpunkt
import net.dankito.banking.fints.messages.datenelemente.implementierte.account.AlleKonten
import net.dankito.banking.fints.messages.datenelemente.implementierte.account.MaximaleAnzahlEintraege
import net.dankito.banking.fints.messages.datenelementgruppen.Datenelementgruppe
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.banking.fints.messages.segmente.Segment
import net.dankito.banking.fints.messages.segmente.id.CustomerSegmentId


open class SaldenabfrageBase(
    segmentNumber: Int,
    segmentVersion: Int,
    account: Datenelementgruppe,
    allAccounts: Boolean = false,
    maxAmountEntries: Int? = null,
    continuationId: String? = null
)
    : Segment(listOf(
        Segmentkopf(CustomerSegmentId.Balance, segmentVersion, segmentNumber),
        account,
        AlleKonten(allAccounts, Existenzstatus.Mandatory),
        MaximaleAnzahlEintraege(maxAmountEntries, Existenzstatus.Optional),
        Aufsetzpunkt(continuationId, Existenzstatus.Optional)
))