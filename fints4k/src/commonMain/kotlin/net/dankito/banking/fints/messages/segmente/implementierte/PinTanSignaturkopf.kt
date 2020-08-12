package net.dankito.banking.fints.messages.segmente.implementierte

import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.*
import net.dankito.banking.fints.model.MessageBaseData


open class PinTanSignaturkopf(
    segmentNumber: Int,
    baseData: MessageBaseData,
    securityControlReference: String,
    date: Int,
    time: Int

) : Signaturkopf(
    segmentNumber,
    baseData.bank,
    baseData.customer,
    baseData.versionOfSecurityProcedure,
    securityControlReference,
    date,
    time,
    SignaturalgorithmusKodiert.PinTanDefaultValue,
    OperationsmodusKodiert.PinTanDefaultValue,
    Schluesselnummer.PinTanDefaultValue,
    Schluesselversion.PinTanDefaultValue
)