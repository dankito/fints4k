package net.dankito.banking.fints.messages.segmente.implementierte.umsaetze

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.abgeleiteteformate.Datum
import net.dankito.banking.fints.messages.datenelemente.basisformate.AlphanumerischesDatenelement
import net.dankito.banking.fints.messages.datenelemente.implementierte.Aufsetzpunkt
import net.dankito.banking.fints.messages.datenelemente.implementierte.account.MaximaleAnzahlEintraege
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.account.Kontoverbindung
import net.dankito.banking.fints.messages.segmente.Segment
import net.dankito.banking.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.banking.fints.model.GetTransactionsParameter


open class KreditkartenUmsaetze(
    segmentNumber: Int,
    parameter: GetTransactionsParameter
) : Segment(listOf(
    Segmentkopf(CustomerSegmentId.CreditCardTransactions, 2, segmentNumber),
    Kontoverbindung(parameter.account),
    AlphanumerischesDatenelement(parameter.account.accountIdentifier, Existenzstatus.Mandatory),
    AlphanumerischesDatenelement(parameter.account.accountIdentifier, Existenzstatus.Mandatory), // TODO: find out what this value really should be; works for Comdirect, but does it work generally?
    Datum(parameter.fromDate, Existenzstatus.Optional),
    Datum(parameter.toDate, Existenzstatus.Optional),
    MaximaleAnzahlEintraege(parameter),
    Aufsetzpunkt(null, Existenzstatus.Optional) // will be set dynamically, see MessageBuilder.rebuildMessageWithContinuationId(); M: vom Institut wurde ein Aufsetzpunkt rückgemeldet. N: sonst
))