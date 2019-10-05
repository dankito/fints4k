package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.Existenzstatus
import net.dankito.fints.messages.datenelemente.implementierte.*
import net.dankito.fints.messages.datenelementgruppen.implementierte.Segmentkopf
import net.dankito.fints.messages.segmente.Segment
import net.dankito.fints.messages.segmente.SegmentId
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData
import net.dankito.fints.model.ProductData


open class Verarbeitungsvorbereitung(
    segmentNumber: Int,
    bank: BankData,
    customer: CustomerData,
    product: ProductData
) : Segment(listOf(
    Segmentkopf(SegmentId.ProcessingPreparation, 3, segmentNumber),
    BPDVersion(bank.bpdVersion, Existenzstatus.Mandatory),
    UPDVersion(customer.updVersion, Existenzstatus.Mandatory),
    DialogspracheDatenelement(customer.selectedLanguage, Existenzstatus.Mandatory),
    Produktbezeichnung(product.name, Existenzstatus.Mandatory),
    Produktversion(product.version, Existenzstatus.Mandatory)
), Existenzstatus.Mandatory)