package net.dankito.fints.messages.segmente.implementierte.umsaetze

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.account.AlleKonten
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.datenelementgruppen.implementierte.account.Kontoverbindung
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData


open class Saldenabfrage(
    segmentNumber: Int,
    bank: BankData,
    customer: CustomerData, // TODO: pass AccountData instead
    allAccounts: Boolean,
    maxAmountEntries: Int? = null
)
    : Segment(listOf(
        Segmentkopf(CustomerSegmentId.Balance, 5, segmentNumber),
        Kontoverbindung(bank.countryCode, bank.bankCode, customer.customerId),
        AlleKonten(allAccounts, Existenzstatus.Mandatory)
//        MaximaleAnzahlEintraege(maxAmountEntries ?: 0, Existenzstatus.Optional),
//        Aufsetzpunkt("", Existenzstatus.Optional)
))