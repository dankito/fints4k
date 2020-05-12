package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.*
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.id.CustomerSegmentId
import net.dankito.fints.model.MessageBaseData


open class Verarbeitungsvorbereitung(
    segmentNumber: Int,
    baseData: MessageBaseData
) : Segment(listOf(
    Segmentkopf(CustomerSegmentId.ProcessingPreparation, 3, segmentNumber),
    BPDVersion(baseData.bank.bpdVersion, Existenzstatus.Mandatory),
    UPDVersion(baseData.customer.updVersion, Existenzstatus.Mandatory),
    DialogspracheDatenelement(baseData.customer.selectedLanguage, Existenzstatus.Mandatory),
    Produktbezeichnung(baseData.product.name, Existenzstatus.Mandatory),
    Produktversion(baseData.product.version, Existenzstatus.Mandatory)
))