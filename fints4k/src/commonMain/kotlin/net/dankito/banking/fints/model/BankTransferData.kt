package net.dankito.banking.fints.model


open class BankTransferData(
    val creditorName: String,
    val creditorIban: String,
    val creditorBic: String,
    val amount: Money,
    val usage: String,
    val instantPayment: Boolean = false
)