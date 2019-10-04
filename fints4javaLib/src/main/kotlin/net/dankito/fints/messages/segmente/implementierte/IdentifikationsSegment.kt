package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.KundenID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatus
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatusWerte
import net.dankito.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment


open class IdentifikationsSegment(
    segmentNumber: Int,
    bankCountryCode: Int,
    bankCode: String,
    customerId: String,
    customerSystemId: String,
    status: KundensystemStatusWerte

) : Segment(listOf(
        Segmentkopf("HKIDN", 2, segmentNumber),
        Kreditinstitutskennung(bankCountryCode, bankCode),
        KundenID(customerId),
        KundensystemID(customerSystemId),
        KundensystemStatus(status, Existenzstatus.Mandatory)
), Existenzstatus.Mandatory)