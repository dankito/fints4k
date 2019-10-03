package net.dankito.fints.messages.nachrichten.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.Dialogsprache
import net.dankito.fints.messages.datenelemente.implementierte.Nachrichtennummer.Companion.FirstMessageNumber
import net.dankito.fints.messages.nachrichten.Nachricht
import net.dankito.fints.messages.segmente.implementierte.IdentifikationsSegment
import net.dankito.fints.messages.segmente.implementierte.Nachrichtenabschluss
import net.dankito.fints.messages.segmente.implementierte.Nachrichtenkopf
import net.dankito.fints.messages.segmente.implementierte.Verarbeitungsvorbereitung


open class Dialoginitialisierung(
    messageSize: Int, // TODO: how to get / calculate size? (give each Segment, Dataelement, ... a size value?)
    bankCountryCode: Int,
    bankCode: String,
    customerId: String,
    customerSystemId: String,
    bpdVersion: Int,
    updVersion: Int,
    language: Dialogsprache,
    productName: String,
    productVersion: String
)
    : Nachricht(listOf(
        Nachrichtenkopf(1, messageSize, "0", FirstMessageNumber),
        IdentifikationsSegment(2, bankCountryCode, bankCode, customerId, customerSystemId),
        Verarbeitungsvorbereitung(3, bpdVersion, updVersion, language, productName, productVersion),
        Nachrichtenabschluss(4, FirstMessageNumber)
))