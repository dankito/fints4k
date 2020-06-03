package net.dankito.banking.fints.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal


open class BankTransferData(
    val creditorName: String,
    val creditorIban: String,
    val creditorBic: String,
    val amount: BigDecimal, // TODO: use Money
    val usage: String,
    val instantPayment: Boolean = false
)