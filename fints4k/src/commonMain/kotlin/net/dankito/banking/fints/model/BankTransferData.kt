package net.dankito.banking.fints.model


open class BankTransferData(
    val recipientName: String,
    val recipientAccountId: String,
    val recipientBankCode: String,
    val amount: Money,
    val reference: String,
    val realTimeTransfer: Boolean = false
)