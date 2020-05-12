package net.dankito.fints.messages.segmente.implementierte

import net.dankito.fints.messages.datenelemente.implementierte.encryption.Komprimierungsfunktion
import net.dankito.fints.messages.datenelemente.implementierte.signatur.OperationsmodusKodiert
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Schluesselart
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Schluesselnummer
import net.dankito.fints.messages.datenelemente.implementierte.signatur.Schluesselversion
import net.dankito.fints.model.MessageBaseData


open class PinTanVerschluesselungskopf(
    baseData: MessageBaseData,
    date: Int,
    time: Int

) : Verschluesselungskopf(
    baseData.bank,
    baseData.customer,
    date,
    time,
    OperationsmodusKodiert.FinTsMockValue,
    Schluesselart.Chiffrierschluessel,
    Schluesselnummer.FinTsMockValue,
    Schluesselversion.FinTsMockValue,
    Komprimierungsfunktion.Keine_Kompression
)