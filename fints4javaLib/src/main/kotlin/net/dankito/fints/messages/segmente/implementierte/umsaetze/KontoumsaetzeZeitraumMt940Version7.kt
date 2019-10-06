package net.dankito.fints.messages.segmente.implementierte.umsaetze

import net.dankito.fints.messages.datenelementgruppen.implementierte.account.KontoverbindungInternational
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData


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
open class KontoumsaetzeZeitraumMt940Version7(
    segmentNumber: Int,
    bank: BankData,
    customer: CustomerData,
    subAccountAttribute: String? = null, // TODO: move to CustomerData.accounts
    allAccounts: Boolean = false,
    fromDate: Int? = null,
    toDate: Int? = null,
    maxAmount: Int? = null,
    continuationId: String? = null
)
    : KontoumsaetzeZeitraumMt940Base(
        7,
        segmentNumber,
        KontoverbindungInternational(bank, customer, subAccountAttribute),
        allAccounts,
        fromDate,
        toDate,
        maxAmount,
        continuationId
    )