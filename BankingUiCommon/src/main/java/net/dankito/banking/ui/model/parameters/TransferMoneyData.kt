package net.dankito.banking.ui.model.parameters

import java.math.BigDecimal


open class TransferMoneyData(
    val creditorName: String,
    val creditorIban: String,
    val creditorBic: String,
    val amount: BigDecimal,
    val usage: String
)