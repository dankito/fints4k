package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.FinTsKundensystemStatus
import net.dankito.fints.messages.datenelemente.implementierte.KundenID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemID
import net.dankito.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment


open class IdentifikationsSegment(
    segmentNumber: Int,
    bankCountryCode: Int,
    bankCode: String,
    customerId: String,
    customerSystemId: String

) : Segment(listOf(
        Segmentkopf("HKIDN", segmentNumber, 2),
        Kreditinstitutskennung(bankCountryCode, bankCode),
        KundenID(customerId),
        KundensystemID(customerSystemId),
        FinTsKundensystemStatus()
), Existenzstatus.Mandatory)