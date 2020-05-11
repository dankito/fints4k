package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.KundenID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemID
import net.dankito.fints.messages.datenelemente.implementierte.KundensystemStatus
import net.dankito.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData


open class IdentifikationsSegment(
    segmentNumber: Int,
    bank: BankData,
    customer: CustomerData

) : Segment(listOf(
        Segmentkopf(CustomerSegmentId.Identification, 2, segmentNumber),
        Kreditinstitutskennung(bank.countryCode, bank.bankCode),
        KundenID(customer.customerId),
        KundensystemID(customer.customerSystemId),
        KundensystemStatus(customer.customerSystemStatus, Existenzstatus.Mandatory)
))