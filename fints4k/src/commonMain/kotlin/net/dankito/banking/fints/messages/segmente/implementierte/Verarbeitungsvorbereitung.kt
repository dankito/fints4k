package net.dankito.banking.fints.messages.segmente.implementierte

import net.dankito.banking.fints.messages.Existenzstatus
import net.dankito.banking.fints.messages.datenelemente.implementierte.*
import net.dankito.banking.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.banking.fints.messages.segmente.Segment
import net.dankito.banking.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.banking.fints.model.MessageBaseData


open class Verarbeitungsvorbereitung(
    segmentNumber: Int,
    baseData: MessageBaseData
) : Segment(listOf(
    Segmentkopf(CustomerSegmentId.ProcessingPreparation, 3, segmentNumber),
    BPDVersion(baseData.bank.bpdVersion, Existenzstatus.Mandatory),
    UPDVersion(baseData.bank.updVersion, Existenzstatus.Mandatory),
    DialogspracheDatenelement(baseData.bank.selectedLanguage, Existenzstatus.Mandatory),
    Produktbezeichnung(baseData.product.name, Existenzstatus.Mandatory),
    Produktversion(baseData.product.version, Existenzstatus.Mandatory)
))