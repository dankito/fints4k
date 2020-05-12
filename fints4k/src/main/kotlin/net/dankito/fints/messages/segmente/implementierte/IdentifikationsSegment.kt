package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.KundenID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatus
import net.dankito.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.fints.model.MessageBaseData


open class IdentifikationsSegment(
    segmentNumber: Int,
    baseData: MessageBaseData

) : Segment(listOf(
        Segmentkopf(CustomerSegmentId.Identification, 2, segmentNumber),
        Kreditinstitutskennung(baseData.bank.countryCode, baseData.bank.bankCode),
        KundenID(baseData.customer.customerId),
        KundensystemID(baseData.customer.customerSystemId),
        KundensystemStatus(baseData.customer.customerSystemStatus, Existenzstatus.Mandatory)
))