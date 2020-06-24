package net.dankito.banking.ui.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import net.dankito.banking.ui.model.tan.TanMedium
import net.dankito.banking.ui.model.tan.TanMediumStatus
import net.dankito.banking.ui.model.tan.TanProcedure
import java.math.BigDecimal
import java.util.*


@JsonIdentityInfo(property = "id", generator = ObjectIdGenerators.PropertyGenerator::class) // to avoid stack overflow due to circular references
open class Customer(
    val bankCode: String,
    val customerId: String,
    var password: String,
    var finTsServerAddress: String,
    var bankName: String,
    var bic: String,
    var customerName: String,
    var userId: String = customerId,
    var iconUrl: String? = null,
    var accounts: List<BankAccount> = listOf()
) {


    internal constructor() : this("", "", "", "", "", "", "") // for object deserializers


    var id: String = UUID.randomUUID().toString()
        protected set


    var supportedTanProcedures: List<TanProcedure> = listOf()

    var selectedTanProcedure: TanProcedure? = null

    var tanMedia: List<TanMedium> = listOf()

    val tanMediaSorted: List<TanMedium>
        get() = tanMedia.sortedByDescending { it.status == TanMediumStatus.Used }


    val displayName: String
        get() = bankName

    val balance: BigDecimal
        get() = accounts.map { it.balance }.fold(BigDecimal.ZERO) { acc, e -> acc + e }

    val transactions: List<AccountTransaction>
        get() = accounts.flatMap { it.bookedTransactions }


    override fun toString(): String {
        return "$customerName ($customerId)"
    }

}