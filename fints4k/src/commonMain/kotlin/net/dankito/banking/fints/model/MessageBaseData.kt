package net.dankito.banking.fints.model


open class MessageBaseData(
    val bank: BankData,
    val customer: CustomerData,
    val product: ProductData
)