package net.dankito.fints.messages.segmente.implementierte.umsaetze

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.abgeleiteteformate.Datum
import net.dankito.fints.messages.datenelemente.implementierte.account.AlleKonten
import net.dankito.fints.messages.datenelementgruppen.Datenelementgruppe
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.CustomerSegmentId


/**
 * Die Lösung bietet dem Kunden die Möglichkeit, auf seinem System verlorengegangene Buchungen erneut zu erhalten.
 *
 * Die maximale Anzahl der rückzumeldenden Buchungspositionen kann begrenzt werden. Eine Buchungsposition besteht
 * aus einem :61:/:86:-Block eines MT 940-Formats. Es muss davon unabhängig immer ein gültiges MT 940-Format
 * zurückgemeldet werden, d.h. die Felder :20: bis :60: und :62: bis :86: sind obligatorischer Bestandteil der Rückmeldung.
 *
 * Der maximale Zeitraum, für den rückwirkend Buchungen beim Kreditinstitut gespeichert sind, wird in den
 * Bankparameterdaten übermittelt.
 */
abstract class KontoumsaetzeZeitraumMt940Base(
    segmentVersion: Int,
    segmentNumber: Int,
    account: Datenelementgruppe,
    allAccounts: Boolean = false,
    fromDate: Int? = null,
    toDate: Int? = null,
    maxAmount: Int? = null,
    continuationId: String? = null
)
    : Segment(listOf(
        Segmentkopf(CustomerSegmentId.AccountTransactionsMt940, segmentVersion, segmentNumber),
        account,
        AlleKonten(allAccounts, Existenzstatus.Mandatory),
        Datum(fromDate ?: 0, Existenzstatus.Optional)
//        Datum(toDate ?: 0, Existenzstatus.Optional),
//        MaximaleAnzahlEintraege(maxAmount ?: 0, Existenzstatus.Optional), // > 0. O: „Eingabe Anzahl Einträge erlaubt“ (BPD) = „J“. N: sonst
//        Aufsetzpunkt(continuationId ?: "", Existenzstatus.Optional) // M: vom Institut wurde ein Aufsetzpunkt rückgemeldet. N: sonst
), Existenzstatus.Mandatory)