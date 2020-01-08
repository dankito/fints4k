package net.dankito.banking.ui.model

import net.dankito.banking.ui.model.tan.TanMedium
import net.dankito.banking.ui.model.tan.TanMediumStatus
import net.dankito.banking.ui.model.tan.TanProcedure
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

    var tanMedia: List<TanMedium> = listOf()

    val tanMediaSorted: List<TanMedium>
        get() = tanMedia.sortedByDescending { it.status == TanMediumStatus.Used }


    val displayName: String
        get() = bank.name

    val balance: BigDecimal
        get() = bankAccounts.map { it.balance }.fold(BigDecimal.ZERO) { acc, e -> acc + e }

    val transactions: List<AccountTransaction>
        get() = bankAccounts.flatMap { it.bookedTransactions }


    override fun toString(): String {
        return "$name ($customerId)"
    }

}