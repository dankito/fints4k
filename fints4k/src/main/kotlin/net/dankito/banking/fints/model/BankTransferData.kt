package net.dankito.banking.fints.model

import java.math.BigDecimal


open class BankTransferData @JvmOverloads constructor(
    val creditorName: String,
    val creditorIban: String,
    val creditorBic: String,
    val amount: BigDecimal,
    val usage: String,
    val instantPayment: Boolean = false
)