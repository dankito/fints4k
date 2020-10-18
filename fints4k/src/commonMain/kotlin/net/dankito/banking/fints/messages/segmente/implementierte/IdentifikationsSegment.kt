package net.dankito.banking.fints.messages.segmente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.implementierte.KundenID
import net.dankito.banking.fints.messages.datenelemente.implementierte.KundensystemID
import net.dankito.banking.fints.messages.datenelemente.implementierte.KundensystemStatus
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Kreditinstitutskennung
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.banking.fints.messages.segmente.Segment
import net.dankito.banking.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.banking.fints.model.BankData
import net.dankito.banking.fints.model.MessageBaseData


open class IdentifikationsSegment(
    segmentNumber: Int,
    bank: BankData

) : Segment(listOf(
        Segmentkopf(CustomerSegmentId.Identification, 2, segmentNumber),
        Kreditinstitutskennung(bank.countryCode, bank.bankCodeForOnlineBanking),
        KundenID(bank.customerId),
        KundensystemID(bank.customerSystemId),
        KundensystemStatus(bank.customerSystemStatus, Existenzstatus.Mandatory)
)) {

    constructor(segmentNumber: Int, baseData: MessageBaseData) : this(segmentNumber, baseData.bank)
}