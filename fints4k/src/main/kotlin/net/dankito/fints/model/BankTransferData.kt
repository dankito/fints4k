package net.dankito.fints.model

import java.math.BigDecimal


open class BankTransferData(
    val creditorName: String,
    val creditorIban: String,
    val creditorBic: String,
    val amount: BigDecimal,
    val usage: String
)