package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.signatur.OperationsmodusKodiert
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Schluesselnummer
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Schluesselversion
import net.dankito.fints.messages.datenelemente.implementierte.signatur.SignaturalgorithmusKodiert
import net.dankito.fints.model.BankData
import net.dankito.fints.model.CustomerData


open class PinTanSignaturkopf(
    segmentNumber: Int,
    bank: BankData,
    customer: CustomerData,
    securityControlReference: String,
    date: Int,
    time: Int

) : Signaturkopf(
    segmentNumber,
    bank,
    customer,
    securityControlReference,
    date,
    time,
    SignaturalgorithmusKodiert.FinTsMockValue,
    OperationsmodusKodiert.FinTsMockValue,
    Schluesselnummer.FinTsMockValue,
    Schluesselversion.FinTsMockValue
)