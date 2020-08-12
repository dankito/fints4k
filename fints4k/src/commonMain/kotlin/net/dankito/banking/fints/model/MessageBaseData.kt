package net.dankito.banking.fints.model

import net.dankito.banking.fints.messages.datenelemente.implementierte.signatur.VersionDesSicherheitsverfahrens


open class MessageBaseData(
    val bank: BankData,
    val customer: CustomerData,
    val product: ProductData,
    val versionOfSecurityProcedure: VersionDesSicherheitsverfahrens = VersionDesSicherheitsverfahrens.PinTanDefaultVersion
)