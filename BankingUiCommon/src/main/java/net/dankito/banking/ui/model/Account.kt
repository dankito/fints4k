package net.dankito.banking.ui.model

import java.math.BigDecimal


open class Account(
    val bank: Bank,
    val customerId: String,
    var pin: String,
    var name: String,
    var userId: String = customerId,
    var bankAccounts: List<BankAccount> = listOf()
) {

    internal constructor() : this(Bank(), "", "", "") // for object deserializers


    var supportedTanProcedures: List<TanProcedure> = listOf()

    var selectedTanProcedure: TanProcedure? = null


    val balance: BigDecimal
        get() = bankAccounts.map { it.balance }.fold(BigDecimal.ZERO) { acc, e -> acc + e }

    val transactions: List<AccountTransaction>
        get() = bankAccounts.flatMap { it.transactions }


    override fun toString(): String {
        return "$name ($customerId)"
    }

}