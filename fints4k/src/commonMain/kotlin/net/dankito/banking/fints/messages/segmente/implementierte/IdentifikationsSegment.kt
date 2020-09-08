package net.dankito.banking.fints.messages.segmente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.implementierte.KundenID
import net.dankito.banking.fints.messages.datenelemente.implementierte.KundensystemID
import net.dankito.banking.fints.messages.datenelemente.implementierte.KundensystemStatus
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.banking.fints.messages.segmente.Segment
import net.dankito.banking.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.banking.fints.model.MessageBaseData


open class IdentifikationsSegment(
    segmentNumber: Int,
    baseData: MessageBaseData

) : Segment(listOf(
        Segmentkopf(CustomerSegmentId.Identification, 2, segmentNumber),
        Kreditinstitutskennung(baseData.bank.countryCode, baseData.bank.bankCode),
        KundenID(baseData.bank.customerId),
        KundensystemID(baseData.bank.customerSystemId),
        KundensystemStatus(baseData.bank.customerSystemStatus, Existenzstatus.Mandatory)
))